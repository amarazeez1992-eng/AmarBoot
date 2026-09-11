//+------------------------------------------------------------------+
//| Grid_Martingale_Basket_v2_RemoteTarget.mq5                       |
//| BOT 1 remote target-symbol execution wrapper                     |
//+------------------------------------------------------------------+
#property copyright "AMAR"
#property version   "2.14"
#property strict

#include <AMAR/AmarBot1CommandReceiver.mqh>

input string InpRemoteTargetSymbol = ""; // blank = chart symbol (backward-compatible)
input int    InpRemotePollSeconds  = 1;
input int    InpRemoteMaxTickAgeMs = 5000;

string g_remoteTargetSymbol = "";
CAmarBot1CommandReceiver g_remoteReceiver;

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
   if(cmd.hasTargetSymbol && !ApplyRemoteTarget(cmd.targetSymbol)) return false;

   switch(cmd.type)
   {
      case AMAR_CMD_START:
         if(!AmarTargetReady(AmarTargetSymbol())) return false;
         IsTrading=true;
         BuildGrid();
         return true;
      case AMAR_CMD_STOP:
         IsTrading=false;
         return true;
      case AMAR_CMD_REBUILD:
         if(!IsTrading || !AmarTargetReady(AmarTargetSymbol())) return false;
         CloseAll(); DeletePending(); ResetCounters(); BuildGrid();
         return true;
      case AMAR_CMD_CLOSE_ALL:
         CloseAll(); DeletePending(); ResetCounters();
         return true;
      case AMAR_CMD_SET_BUY_ENABLED:
         if(!cmd.hasEnabled) return false;
         BuyEnabled=cmd.enabled;
         if(IsTrading && AmarTargetReady(AmarTargetSymbol())) { CloseAll(); DeletePending(); ResetCounters(); BuildGrid(); }
         return true;
      case AMAR_CMD_SET_SELL_ENABLED:
         if(!cmd.hasEnabled) return false;
         SellEnabled=cmd.enabled;
         if(IsTrading && AmarTargetReady(AmarTargetSymbol())) { CloseAll(); DeletePending(); ResetCounters(); BuildGrid(); }
         return true;
      case AMAR_CMD_UPDATE_SETTINGS:
         if(!cmd.hasSettings || !cmd.hasBuyEnabled || !cmd.hasSellEnabled) return false;
         if(!AmarTargetReady(AmarTargetSymbol())) return false;
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
         return false;
   }
}

void OnTick()
{
   if(_Symbol==AmarTargetSymbol()) AmarOriginalOnTick();
}

void OnTimer()
{
   AmarBot1RemoteCommand cmd;
   if(g_remoteReceiver.Read(cmd))
   {
      bool ok=ApplyRemoteCommand(cmd);
      g_remoteReceiver.Ack(cmd.requestId,ok,ok?"VERIFIED":"FAILED_FAIL_CLOSED");
      Print(ok ? "AMAR remote command verified" : "AMAR remote command rejected/fail-closed");
      ChartRedraw(0);
   }
   if(_Symbol!=AmarTargetSymbol()) AmarTargetCycle();
}

int OnInit()
{
   g_remoteTargetSymbol=InpRemoteTargetSymbol;
   if(StringLen(g_remoteTargetSymbol)>0 && !AmarTargetReady(g_remoteTargetSymbol))
   {
      Print("AMAR FAIL-CLOSED: configured target symbol unavailable or market unhealthy: ",g_remoteTargetSymbol);
      return INIT_FAILED;
   }
   int result=AmarOriginalOnInit();
   if(result!=INIT_SUCCEEDED) return result;
   int seconds=InpRemotePollSeconds;
   if(seconds<1) seconds=1;
   if(!EventSetTimer(seconds))
   {
      Print("AMAR FAIL-CLOSED: EventSetTimer failed. Error=",GetLastError());
      return INIT_FAILED;
   }
   Print("AMAR remote target control armed for: ",AmarTargetSymbol());
   return INIT_SUCCEEDED;
}

void OnDeinit(const int reason)
{
   EventKillTimer();
   AmarOriginalOnDeinit(reason);
}
