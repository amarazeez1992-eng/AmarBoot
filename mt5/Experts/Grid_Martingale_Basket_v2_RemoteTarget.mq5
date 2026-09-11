//+------------------------------------------------------------------+
//| Grid_Martingale_Basket_v2_RemoteTarget.mq5                       |
//| BOT 1 remote target-symbol execution wrapper                     |
//+------------------------------------------------------------------+
#property copyright "AMAR"
#property version   "2.17"
#property strict

#include <AMAR/AmarBot1CommandReceiver.mqh>

#define AMAR_BOT1_STATE_FILE "AMAR_BOT1_STATE.json"
#define AMAR_BOT1_STRATEGY_ID "STRATEGY_01"
#define AMAR_BOT1_STRATEGY_VERSION "2.00"

input string InpRemoteTargetSymbol = "";
input int    InpRemotePollSeconds  = 1;
input int    InpRemoteMaxTickAgeMs = 5000;

string g_remoteTargetSymbol = "";
CAmarBot1CommandReceiver g_remoteReceiver;
string g_lastRequestId = "";
string g_lastCommandStatus = "IDLE";
string g_lastError = "";

string AmarTargetSymbol()
{
   if(StringLen(g_remoteTargetSymbol) > 0) return g_remoteTargetSymbol;
   if(StringLen(InpRemoteTargetSymbol) > 0) return InpRemoteTargetSymbol;
   return _Symbol;
}

bool AmarTargetReady(string symbol)
{
   if(StringLen(symbol) <= 0) return false;
   if(!SymbolSelect(symbol,true)) return false;
   if(!SymbolIsSynchronized(symbol)) return false;
   MqlTick tick;
   if(!SymbolInfoTick(symbol,tick)) return false;
   if(!MathIsValidNumber(tick.bid) || !MathIsValidNumber(tick.ask)) return false;
   if(tick.bid <= 0 || tick.ask <= 0 || tick.ask < tick.bid) return false;
   int maxAge=InpRemoteMaxTickAgeMs;
   if(maxAge<0) maxAge=0;
   if(maxAge>0 && tick.time_msc>0)
   {
      long nowMs=(long)TimeCurrent()*1000;
      long age=nowMs-(long)tick.time_msc;
      if(age>maxAge || age<0) return false;
   }
   return true;
}

int AmarPendingCount()
{
   int count=0;
   for(int i=0;i<OrdersTotal();i++)
   {
      ulong ticket=OrderGetTicket(i);
      if(ticket==0 || !OrderSelect(ticket)) continue;
      if(OrderGetInteger(ORDER_MAGIC)!=Magic) continue;
      if(OrderGetString(ORDER_SYMBOL)!=AmarTargetSymbol()) continue;
      count++;
   }
   return count;
}

int AmarSideCount(ENUM_POSITION_TYPE side)
{
   int count=0;
   string symbol=AmarTargetSymbol();
   for(int i=0;i<PositionsTotal();i++)
   {
      ulong ticket=PositionGetTicket(i);
      if(ticket==0 || !PositionSelectByTicket(ticket)) continue;
      if(PositionGetInteger(POSITION_MAGIC)!=Magic) continue;
      if(PositionGetString(POSITION_SYMBOL)!=symbol) continue;
      if((ENUM_POSITION_TYPE)PositionGetInteger(POSITION_TYPE)==side) count++;
   }
   return count;
}

ENUM_ORDER_TYPE_FILLING AmarFillingType(string symbol)
{
   long mode=SymbolInfoInteger(symbol,SYMBOL_FILLING_MODE);
   if((mode & SYMBOL_FILLING_IOC)==SYMBOL_FILLING_IOC) return ORDER_FILLING_IOC;
   if((mode & SYMBOL_FILLING_FOK)==SYMBOL_FILLING_FOK) return ORDER_FILLING_FOK;
   return ORDER_FILLING_RETURN;
}

bool AmarCloseSide(ENUM_POSITION_TYPE side)
{
   string symbol=AmarTargetSymbol();
   if(!AmarTargetReady(symbol))
   {
      g_lastError="MARKET_NOT_READY";
      return false;
   }

   bool allClosed=true;
   for(int i=PositionsTotal()-1;i>=0;i--)
   {
      ulong ticket=PositionGetTicket(i);
      if(ticket==0 || !PositionSelectByTicket(ticket)) continue;
      if(PositionGetInteger(POSITION_MAGIC)!=Magic) continue;
      if(PositionGetString(POSITION_SYMBOL)!=symbol) continue;
      ENUM_POSITION_TYPE positionSide=(ENUM_POSITION_TYPE)PositionGetInteger(POSITION_TYPE);
      if(positionSide!=side) continue;

      double volume=PositionGetDouble(POSITION_VOLUME);
      if(!MathIsValidNumber(volume) || volume<=0.0)
      {
         allClosed=false;
         g_lastError="INVALID_POSITION_VOLUME";
         continue;
      }

      MqlTick tick;
      if(!SymbolInfoTick(symbol,tick))
      {
         allClosed=false;
         g_lastError="TICK_UNAVAILABLE";
         continue;
      }

      MqlTradeRequest req={};
      MqlTradeResult res={};
      req.action=TRADE_ACTION_DEAL;
      req.position=ticket;
      req.symbol=symbol;
      req.volume=volume;
      req.type=(side==POSITION_TYPE_BUY) ? ORDER_TYPE_SELL : ORDER_TYPE_BUY;
      req.price=(side==POSITION_TYPE_BUY) ? tick.bid : tick.ask;
      req.deviation=20;
      req.magic=Magic;
      req.type_filling=AmarFillingType(symbol);
      req.comment=(side==POSITION_TYPE_BUY) ? "AMAR_CLOSE_BUY" : "AMAR_CLOSE_SELL";

      if(!OrderSend(req,res) || (res.retcode!=TRADE_RETCODE_DONE && res.retcode!=TRADE_RETCODE_DONE_PARTIAL))
      {
         allClosed=false;
         g_lastError=StringFormat("SIDE_CLOSE_FAILED_%u",res.retcode);
         Print("AMAR side close failed ticket=",ticket," retcode=",res.retcode);
      }
   }

   if(AmarSideCount(side)>0)
   {
      allClosed=false;
      if(g_lastError=="") g_lastError="SIDE_POSITIONS_REMAIN";
   }
   return allClosed;
}

double AmarFloatingProfitLoss()
{
   double total=0.0;
   string symbol=AmarTargetSymbol();
   for(int i=0;i<PositionsTotal();i++)
   {
      ulong ticket=PositionGetTicket(i);
      if(ticket==0 || !PositionSelectByTicket(ticket)) continue;
      if(PositionGetInteger(POSITION_MAGIC)!=Magic) continue;
      if(PositionGetString(POSITION_SYMBOL)!=symbol) continue;
      double profit=PositionGetDouble(POSITION_PROFIT);
      if(MathIsValidNumber(profit)) total+=profit;
   }
   return total;
}

void AmarWriteState()
{
   string symbol=AmarTargetSymbol();
   bool marketReady=AmarTargetReady(symbol);
   int h=FileOpen(AMAR_BOT1_STATE_FILE,FILE_WRITE|FILE_TXT|FILE_COMMON|FILE_ANSI|FILE_SHARE_READ);
   if(h==INVALID_HANDLE) return;

   string safeError=g_lastError;
   StringReplace(safeError,"\"","'");
   StringReplace(safeError,"\r"," ");
   StringReplace(safeError,"\n"," ");
   string safeRequest=g_lastRequestId;
   StringReplace(safeRequest,"\"","'");
   string state=IsTrading ? "RUNNING" : "OFF";
   string payload=StringFormat(
      "{\"bot_id\":\"BOT_1\",\"magic\":%d,\"strategy_id\":\"%s\",\"strategy_version\":\"%s\",\"runtime_state\":\"%s\",\"target_symbol\":\"%s\",\"chart_symbol\":\"%s\",\"is_trading\":%s,\"buy_enabled\":%s,\"sell_enabled\":%s,\"lot_start\":%.8f,\"grid_step\":%d,\"max_orders\":%d,\"martingale\":%.8f,\"basket_tp\":%.8f,\"basket_sl\":%.8f,\"trailing\":%d,\"open_positions\":%d,\"buy_positions\":%d,\"sell_positions\":%d,\"pending_orders\":%d,\"floating_profit_loss\":%.8f,\"market_ready\":%s,\"heartbeat_ms\":%I64d,\"last_request_id\":\"%s\",\"last_command_status\":\"%s\",\"last_error\":\"%s\"}",
      Magic,AMAR_BOT1_STRATEGY_ID,AMAR_BOT1_STRATEGY_VERSION,state,symbol,_Symbol,IsTrading?"true":"false",BuyEnabled?"true":"false",SellEnabled?"true":"false",
      LotStart,GridStep,MaxOrders,Martingale,BasketTP,BasketSL,Trail,CountBotPositions(),AmarSideCount(POSITION_TYPE_BUY),AmarSideCount(POSITION_TYPE_SELL),AmarPendingCount(),AmarFloatingProfitLoss(),marketReady?"true":"false",
      (long)TimeCurrent()*1000,safeRequest,g_lastCommandStatus,safeError);
   FileWriteString(h,payload+"\n");
   FileFlush(h);
   FileClose(h);
}

#define Symbol() AmarTargetSymbol()
#define OnInit AmarOriginalOnInit
#define OnDeinit AmarOriginalOnDeinit
#define OnTick AmarOriginalOnTick
#include "Grid_Martingale_Basket_v2.mq5"
#undef OnTick
#undef OnDeinit
#undef OnInit
#undef Symbol

void AmarTargetCycle()
{
   if(!IsTrading) return;
   if(!AmarTargetReady(AmarTargetSymbol())) return;
   CheckBasket();
   if(Trail > 0) ManageTrailing();
   TrackPrice();
}

bool ApplyRemoteTarget(string requested)
{
   if(StringLen(requested) <= 0) return false;
   if(!AmarTargetReady(requested))
   {
      g_lastError="TARGET_MARKET_UNHEALTHY";
      Print("AMAR FAIL-CLOSED: target symbol unavailable or market unhealthy: ",requested);
      return false;
   }
   string oldTarget=AmarTargetSymbol();
   if(oldTarget==requested) return true;
   DeletePending();
   CloseAll();
   ResetCounters();
   g_remoteTargetSymbol=requested;
   Print("AMAR target symbol switched: ",oldTarget," -> ",g_remoteTargetSymbol);
   return true;
}

bool ApplyRemoteCommand(const AmarBot1RemoteCommand &cmd)
{
   g_lastError="";
   if(cmd.hasTargetSymbol && !ApplyRemoteTarget(cmd.targetSymbol)) return false;

   switch(cmd.type)
   {
      case AMAR_CMD_START:
         if(!AmarTargetReady(AmarTargetSymbol())) { g_lastError="MARKET_NOT_READY"; return false; }
         IsTrading=true;
         BuildGrid();
         return true;
      case AMAR_CMD_STOP:
         IsTrading=false;
         return true;
      case AMAR_CMD_REBUILD:
         if(!IsTrading || !AmarTargetReady(AmarTargetSymbol())) { g_lastError="REBUILD_GATE_BLOCKED"; return false; }
         CloseAll(); DeletePending(); ResetCounters(); BuildGrid();
         return true;
      case AMAR_CMD_CLOSE_ALL:
         CloseAll(); DeletePending(); ResetCounters();
         return true;
      case AMAR_CMD_CLOSE_BUY:
         return AmarCloseSide(POSITION_TYPE_BUY);
      case AMAR_CMD_CLOSE_SELL:
         return AmarCloseSide(POSITION_TYPE_SELL);
      case AMAR_CMD_SET_BUY_ENABLED:
         if(!cmd.hasEnabled) { g_lastError="INVALID_BUY_SETTING"; return false; }
         BuyEnabled=cmd.enabled;
         if(IsTrading && AmarTargetReady(AmarTargetSymbol())) { CloseAll(); DeletePending(); ResetCounters(); BuildGrid(); }
         return true;
      case AMAR_CMD_SET_SELL_ENABLED:
         if(!cmd.hasEnabled) { g_lastError="INVALID_SELL_SETTING"; return false; }
         SellEnabled=cmd.enabled;
         if(IsTrading && AmarTargetReady(AmarTargetSymbol())) { CloseAll(); DeletePending(); ResetCounters(); BuildGrid(); }
         return true;
      case AMAR_CMD_UPDATE_SETTINGS:
         if(!cmd.hasSettings || !cmd.hasBuyEnabled || !cmd.hasSellEnabled) { g_lastError="INVALID_SETTINGS"; return false; }
         if(!AmarTargetReady(AmarTargetSymbol())) { g_lastError="MARKET_NOT_READY"; return false; }
         LotStart=cmd.lotStart;
         GridStep=(int)cmd.gridStep;
         MaxOrders=cmd.maxOrders;
         Martingale=cmd.martingale;
         BasketTP=cmd.basketTp;
         BasketSL=cmd.basketSl;
         Trail=(int)cmd.trailing;
         BuyEnabled=cmd.buyEnabled;
         SellEnabled=cmd.sellEnabled;
         if(IsTrading) { CloseAll(); DeletePending(); ResetCounters(); BuildGrid(); }
         return true;
      default:
         g_lastError="UNSUPPORTED_COMMAND";
         return false;
   }
}

void OnTick()
{
   if(_Symbol==AmarTargetSymbol()) AmarOriginalOnTick();
   AmarWriteState();
}

void OnTimer()
{
   AmarBot1RemoteCommand cmd;
   if(g_remoteReceiver.Read(cmd))
   {
      g_lastRequestId=cmd.requestId;
      bool ok=ApplyRemoteCommand(cmd);
      g_lastCommandStatus=ok ? "VERIFIED" : "FAILED";
      if(!ok && g_lastError=="") g_lastError="FAILED_FAIL_CLOSED";
      g_remoteReceiver.Ack(cmd.requestId,ok,ok?"VERIFIED":g_lastError);
      Print(ok ? "AMAR remote command verified" : "AMAR remote command rejected/fail-closed");
      ChartRedraw(0);
   }
   if(_Symbol!=AmarTargetSymbol()) AmarTargetCycle();
   AmarWriteState();
}

int OnInit()
{
   g_remoteTargetSymbol=InpRemoteTargetSymbol;
   g_lastError="";
   if(StringLen(g_remoteTargetSymbol)>0 && !AmarTargetReady(g_remoteTargetSymbol))
   {
      g_lastError="INITIAL_TARGET_NOT_READY";
      AmarWriteState();
      Print("AMAR FAIL-CLOSED: configured target symbol unavailable or market unhealthy: ",g_remoteTargetSymbol);
      return INIT_FAILED;
   }
   int result=AmarOriginalOnInit();
   if(result!=INIT_SUCCEEDED) return result;
   int seconds=InpRemotePollSeconds;
   if(seconds<1) seconds=1;
   if(!EventSetTimer(seconds))
   {
      g_lastError="TIMER_INIT_FAILED";
      AmarWriteState();
      Print("AMAR FAIL-CLOSED: EventSetTimer failed. Error=",GetLastError());
      return INIT_FAILED;
   }
   g_lastCommandStatus="ARMED";
   AmarWriteState();
   Print("AMAR remote target control armed for: ",AmarTargetSymbol());
   return INIT_SUCCEEDED;
}

void OnDeinit(const int reason)
{
   EventKillTimer();
   g_lastCommandStatus="STOPPED";
   AmarWriteState();
   AmarOriginalOnDeinit(reason);
}