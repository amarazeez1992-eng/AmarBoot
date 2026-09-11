//+------------------------------------------------------------------+
//| Grid_Martingale_Basket_v2_RemoteTarget.mq5                       |
//| BOT 1 remote target-symbol execution wrapper                     |
//|                                                                  |
//| The authoritative strategy source remains untouched. This EA     |
//| includes it and routes its existing Symbol() calls through an    |
//| explicit, verified target symbol selected remotely.              |
//+------------------------------------------------------------------+
#property copyright "AMAR"
#property version   "2.11"
#property strict

#include <AMAR/AmarBot1CommandReceiver.mqh>

input string InpRemoteTargetSymbol = ""; // blank = chart symbol (backward-compatible)
input int    InpRemotePollSeconds  = 1;

string g_remoteTargetSymbol = "";
CAmarBot1CommandReceiver g_remoteReceiver;

string AmarTargetSymbol()
{
   if(StringLen(g_remoteTargetSymbol) > 0)
      return g_remoteTargetSymbol;
   if(StringLen(InpRemoteTargetSymbol) > 0)
      return InpRemoteTargetSymbol;
   return _Symbol;
}

bool AmarTargetReady(string symbol)
{
   if(StringLen(symbol) <= 0) return false;
   if(!SymbolSelect(symbol,true)) return false;

   MqlTick tick;
   if(!SymbolInfoTick(symbol,tick)) return false;
   if(!MathIsValidNumber(tick.bid) || !MathIsValidNumber(tick.ask)) return false;
   if(tick.bid <= 0 || tick.ask <= 0 || tick.ask < tick.bid) return false;
   return true;
}

// Route the unchanged strategy's Symbol() calls to the verified target.
#define Symbol() AmarTargetSymbol()
#include "Grid_Martingale_Basket_v2.mq5"
#undef Symbol

bool ApplyRemoteTarget(string requested)
{
   if(StringLen(requested) <= 0) return false;
   if(!AmarTargetReady(requested))
   {
      Print("AMAR FAIL-CLOSED: target symbol unavailable: ",requested);
      return false;
   }

   string oldTarget=AmarTargetSymbol();
   if(oldTarget==requested) return true;

   // Close/delete only BOT 1 exposure belonging to the old target before
   // changing execution context. This prevents accidental cross-symbol use.
   DeletePending();
   CloseAll();
   ResetCounters();
   g_remoteTargetSymbol=requested;
   Print("AMAR target symbol switched: ",oldTarget," -> ",g_remoteTargetSymbol);
   return true;
}

bool ApplyRemoteCommand(const AmarBot1RemoteCommand &cmd)
{
   if(cmd.hasTargetSymbol && !ApplyRemoteTarget(cmd.targetSymbol))
      return false;

   switch(cmd.type)
   {
      case AMAR_CMD_START:
         if(!AmarTargetReady(AmarTargetSymbol())) return false;
         IsTrading=true;
         BuildGrid();
         return true;

      case AMAR_CMD_STOP:
         // BOT OFF is an engine stop only: do not close positions or delete orders.
         IsTrading=false;
         return true;

      case AMAR_CMD_REBUILD:
         if(!IsTrading || !AmarTargetReady(AmarTargetSymbol())) return false;
         CloseAll();
         DeletePending();
         ResetCounters();
         BuildGrid();
         return true;

      case AMAR_CMD_CLOSE_ALL:
         CloseAll();
         DeletePending();
         ResetCounters();
         return true;

      case AMAR_CMD_SET_BUY_ENABLED:
         if(!cmd.hasEnabled) return false;
         BuyEnabled=cmd.enabled;
         if(IsTrading && AmarTargetReady(AmarTargetSymbol()))
         {
            CloseAll(); DeletePending(); ResetCounters(); BuildGrid();
         }
         return true;

      case AMAR_CMD_SET_SELL_ENABLED:
         if(!cmd.hasEnabled) return false;
         SellEnabled=cmd.enabled;
         if(IsTrading && AmarTargetReady(AmarTargetSymbol()))
         {
            CloseAll(); DeletePending(); ResetCounters(); BuildGrid();
         }
         return true;

      case AMAR_CMD_UPDATE_SETTINGS:
         if(!cmd.hasSettings || !AmarTargetReady(AmarTargetSymbol())) return false;
         LotStart=cmd.lotStart;
         GridStep=(int)cmd.gridStep;
         MaxOrders=cmd.maxOrders;
         Martingale=cmd.martingale;
         BasketTP=cmd.basketTp;
         BasketSL=cmd.basketSl;
         Trail=(int)cmd.trailing;
         BuyEnabled=cmd.enabled;
         SellEnabled=cmd.hasEnabled ? cmd.enabled : SellEnabled;
         if(IsTrading)
         {
            CloseAll(); DeletePending(); ResetCounters(); BuildGrid();
         }
         return true;

      default:
         return false;
   }
}

void OnTimer()
{
   AmarBot1RemoteCommand cmd;
   if(g_remoteReceiver.Read(cmd))
   {
      bool ok=ApplyRemoteCommand(cmd);
      Print(ok ? "AMAR remote command verified" : "AMAR remote command rejected/fail-closed");
      ChartRedraw(0);
   }
}

int OnInitRemoteTarget()
{
   g_remoteTargetSymbol=InpRemoteTargetSymbol;
   int seconds=InpRemotePollSeconds;
   if(seconds<1) seconds=1;
   EventSetTimer(seconds);
   return INIT_SUCCEEDED;
}

void OnDeinitRemoteTarget()
{
   EventKillTimer();
}
