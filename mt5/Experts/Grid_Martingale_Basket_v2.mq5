//+------------------------------------------------------------------+
//| Grid_Martingale_Basket_v2.mq5                                   |
//| AMAR BOT 1 - Grid + Martingale + Basket                         |
//| Hardened B35 baseline: BOT1-only scope, fail-safe defaults,     |
//| explicit runtime state, broker retcode validation.              |
//+------------------------------------------------------------------+
#property copyright "AMAR"
#property version   "2.10"
#property strict

// -----------------------------------------------------------------
// IMPORTANT SAFETY CONTRACT
// - All numeric inputs default to 0 and trading directions default OFF.
// - BOT OFF stops new BOT1 activity; it does NOT close positions or
//   delete pending orders.
// - REBUILD only removes BOT1 pending orders and rebuilds BOT1 grid.
// - CLOSE ALL only affects BOT1 positions/pending orders on this symbol.
// - Basket, trailing and position counts are BOT1-only.
// - Manual trades and other EAs are never included.
// -----------------------------------------------------------------

input double InpLotStart   = 0.0;   // Initial lot; 0 = disabled
input int    InpGridStep   = 0;     // Grid distance in points; 0 = disabled
input int    InpMaxOrders  = 0;     // Maximum BOT1 positions + pending orders
input double InpMartingale = 0.0;   // Lot multiplier; 0 = disabled
input double InpBasketTP   = 0.0;   // Basket profit in account currency; 0 = disabled
input double InpBasketSL   = 0.0;   // Basket loss as positive amount; 0 = disabled
input int    InpTrail      = 0;     // Trailing distance in points; 0 = disabled
input bool   InpEnableBuy  = false;
input bool   InpEnableSell = false;

const long   BOT1_MAGIC = 20260908;
const string PREFIX = "GBM2_";

enum AMAR_BOT_STATE
  {
   BOT_OFF=0,
   BOT_STARTING,
   BOT_RUNNING,
   BOT_STOPPING,
   BOT_REBUILDING,
   BOT_CLOSING,
   BOT_ERROR,
   BOT_EMERGENCY_LOCK
  };

AMAR_BOT_STATE BotState = BOT_OFF;

bool   BuyEnabled  = false;
bool   SellEnabled = false;
double LotStart    = 0.0;
int    GridStep    = 0;
int    MaxOrders   = 0;
double Martingale  = 0.0;
double BasketTP    = 0.0;
double BasketSL    = 0.0;
int    Trail        = 0;

double LastBuyPrice  = 0.0;
double LastSellPrice = 0.0;
int    BuyStepCount  = 0;
int    SellStepCount = 0;

//+------------------------------------------------------------------+
//| Initialization                                                   |
//+------------------------------------------------------------------+
int OnInit()
  {
   LotStart    = InpLotStart;
   GridStep    = InpGridStep;
   MaxOrders   = InpMaxOrders;
   Martingale  = InpMartingale;
   BasketTP    = InpBasketTP;
   BasketSL    = InpBasketSL;
   Trail       = InpTrail;
   BuyEnabled  = InpEnableBuy;
   SellEnabled = InpEnableSell;

   CreateUI();

   if(!ConfigurationIsTradable())
     {
      BotState=BOT_OFF;
      Print("AMAR BOT1: disabled. Configure positive numeric values and enable BUY and/or SELL.");
      UpdateUI();
      return(INIT_SUCCEEDED);
     }

   BotState=BOT_STARTING;
   ResetCounters();
   BuildGrid();

   if(BotState!=BOT_ERROR)
      BotState=BOT_RUNNING;

   UpdateUI();
   return(INIT_SUCCEEDED);
  }

void OnDeinit(const int reason)
  {
   ObjectsDeleteAll(0,PREFIX);
   Comment("");
  }

//+------------------------------------------------------------------+
//| Main runtime                                                    |
//+------------------------------------------------------------------+
void OnTick()
  {
   if(BotState==BOT_EMERGENCY_LOCK || BotState==BOT_ERROR)
      return;

   if(BotState==BOT_OFF)
      return;

   if(BotState!=BOT_RUNNING)
      return;

   CheckBasket();

   if(BotState!=BOT_RUNNING)
      return;

   if(Trail>0)
      ManageTrailing();

   TrackPrice();
  }

//+------------------------------------------------------------------+
//| Chart controls                                                   |
//+------------------------------------------------------------------+
void OnChartEvent(const int id,const long &lparam,const double &dparam,const string &sparam)
  {
   if(id==CHARTEVENT_OBJECT_CLICK)
     {
      if(sparam==PREFIX+"btnClose")
        {
         CloseAll();
         return;
        }

      if(sparam==PREFIX+"btnRebuild")
        {
         if(BotState!=BOT_EMERGENCY_LOCK)
            RebuildGrid();
         return;
        }

      if(sparam==PREFIX+"btnToggle")
        {
         if(BotState==BOT_EMERGENCY_LOCK)
            return;

         if(BotState==BOT_RUNNING)
            SetBotOff();
         else if(BotState==BOT_OFF && ConfigurationIsTradable())
            StartBot();
         return;
        }

      if(sparam==PREFIX+"btnBuy")
        {
         BuyEnabled=!BuyEnabled;
         UpdateUI();
         if(BotState==BOT_RUNNING)
            RebuildGrid();
         return;
        }

      if(sparam==PREFIX+"btnSell")
        {
         SellEnabled=!SellEnabled;
         UpdateUI();
         if(BotState==BOT_RUNNING)
            RebuildGrid();
         return;
        }
     }

   if(id==CHARTEVENT_OBJECT_ENDEDIT)
     {
      string val=ObjectGetString(0,sparam,OBJPROP_TEXT);
      bool rebuild=false;

      if(sparam==PREFIX+"editLot")
        { LotStart=StringToDouble(val); rebuild=true; }
      else if(sparam==PREFIX+"editStep")
        { GridStep=(int)StringToInteger(val); rebuild=true; }
      else if(sparam==PREFIX+"editMax")
        { MaxOrders=(int)StringToInteger(val); rebuild=true; }
      else if(sparam==PREFIX+"editMart")
        { Martingale=StringToDouble(val); rebuild=true; }
      else if(sparam==PREFIX+"editTP")
        { BasketTP=StringToDouble(val); }
      else if(sparam==PREFIX+"editSL")
        { BasketSL=StringToDouble(val); }
      else if(sparam==PREFIX+"editTrail")
        { Trail=(int)StringToInteger(val); }

      if(!ConfigurationIsValid())
        {
         BotState=BOT_OFF;
         Print("AMAR BOT1: invalid configuration; runtime forced OFF.");
        }
      else if(rebuild && BotState==BOT_RUNNING)
         RebuildGrid();

      UpdateUI();
     }
  }

//+------------------------------------------------------------------+
//| Runtime controls                                                 |
//+------------------------------------------------------------------+
void StartBot()
  {
   if(!ConfigurationIsTradable())
      return;

   BotState=BOT_STARTING;
   ResetCounters();
   BuildGrid();
   if(BotState!=BOT_ERROR)
      BotState=BOT_RUNNING;
   UpdateUI();
  }

void SetBotOff()
  {
   // Deliberately do NOT close positions or delete pending orders.
   BotState=BOT_STOPPING;
   BotState=BOT_OFF;
   UpdateUI();
   Print("AMAR BOT1: OFF. Existing BOT1 positions/pending orders were preserved.");
  }

void RebuildGrid()
  {
   if(BotState==BOT_EMERGENCY_LOCK || !ConfigurationIsTradable())
      return;

   BotState=BOT_REBUILDING;
   UpdateUI();

   DeletePending();
   ResetCounters();
   BuildGridOnly();

   if(BotState!=BOT_ERROR)
      BotState=BOT_RUNNING;
   UpdateUI();
  }

//+------------------------------------------------------------------+
//| Configuration                                                    |
//+------------------------------------------------------------------+
bool ConfigurationIsValid()
  {
   if(!MathIsValidNumber(LotStart) || LotStart<=0.0) return false;
   if(GridStep<=0) return false;
   if(MaxOrders<=0) return false;
   if(!MathIsValidNumber(Martingale) || Martingale<=0.0) return false;
   if(!MathIsValidNumber(BasketTP) || BasketTP<0.0) return false;
   if(!MathIsValidNumber(BasketSL) || BasketSL<0.0) return false;
   if(Trail<0) return false;
   if(!BuyEnabled && !SellEnabled) return false;
   return true;
  }

bool ConfigurationIsTradable()
  {
   return ConfigurationIsValid() && (BuyEnabled || SellEnabled);
  }

//+------------------------------------------------------------------+
//| BOT1-scoped counting                                             |
//+------------------------------------------------------------------+
bool IsBot1Position(ulong ticket)
  {
   if(!PositionSelectByTicket(ticket)) return false;
   return PositionGetInteger(POSITION_MAGIC)==BOT1_MAGIC &&
          PositionGetString(POSITION_SYMBOL)==Symbol();
  }

bool IsBot1Order(ulong ticket)
  {
   if(!OrderSelect(ticket)) return false;
   return OrderGetInteger(ORDER_MAGIC)==BOT1_MAGIC &&
          OrderGetString(ORDER_SYMBOL)==Symbol();
  }

int Bot1PositionCount()
  {
   int count=0;
   for(int i=0;i<PositionsTotal();i++)
     {
      ulong ticket=PositionGetTicket(i);
      if(IsBot1Position(ticket)) count++;
     }
   return count;
  }

int Bot1PendingCount()
  {
   int count=0;
   for(int i=0;i<OrdersTotal();i++)
     {
      ulong ticket=OrderGetTicket(i);
      if(IsBot1Order(ticket)) count++;
     }
   return count;
  }

//+------------------------------------------------------------------+
//| Grid                                                              |
//+------------------------------------------------------------------+
void BuildGrid()
  {
   if(!ConfigurationIsTradable())
     {
      BotState=BOT_OFF;
      return;
     }

   DeletePending();
   ResetCounters();
   BuildGridOnly();
  }

void BuildGridOnly()
  {
   if(!ConfigurationIsTradable())
     {
      BotState=BOT_OFF;
      return;
     }

   double point=SymbolInfoDouble(Symbol(),SYMBOL_POINT);
   double bid=SymbolInfoDouble(Symbol(),SYMBOL_BID);
   double ask=SymbolInfoDouble(Symbol(),SYMBOL_ASK);
   if(point<=0.0 || bid<=0.0 || ask<=0.0)
     {
      BotState=BOT_ERROR;
      Print("AMAR BOT1: invalid market data; grid build blocked.");
      return;
     }

   double step=GridStep*point;
   int perSide=MaxOrders/2;
   if(perSide<=0) perSide=1;

   if(BuyEnabled)
     {
      for(int i=1;i<=perSide;i++)
        {
         if(Bot1PositionCount()+Bot1PendingCount()>=MaxOrders) break;
         double price=NormalizePrice(ask+(i*step));
         double lot=GetLotSize(i);
         PlacePendingOrder(ORDER_TYPE_BUY_STOP,price,lot,"BUY_STOP_"+IntegerToString(i));
        }
     }

   if(SellEnabled)
     {
      for(int i=1;i<=perSide;i++)
        {
         if(Bot1PositionCount()+Bot1PendingCount()>=MaxOrders) break;
         double price=NormalizePrice(bid-(i*step));
         double lot=GetLotSize(i);
         PlacePendingOrder(ORDER_TYPE_SELL_STOP,price,lot,"SELL_STOP_"+IntegerToString(i));
        }
     }

   Print("AMAR BOT1: grid build complete. Pending=",Bot1PendingCount()," Positions=",Bot1PositionCount());
  }

//+------------------------------------------------------------------+
//| Lot calculation                                                  |
//+------------------------------------------------------------------+
double GetLotSize(int stepNumber)
  {
   if(stepNumber<1) stepNumber=1;
   double lot=LotStart*MathPow(Martingale,stepNumber-1);

   double minLot=SymbolInfoDouble(Symbol(),SYMBOL_VOLUME_MIN);
   double maxLot=SymbolInfoDouble(Symbol(),SYMBOL_VOLUME_MAX);
   double stepLot=SymbolInfoDouble(Symbol(),SYMBOL_VOLUME_STEP);
   if(minLot<=0.0) minLot=0.01;
   if(maxLot<=0.0) maxLot=100.0;
   if(stepLot<=0.0) stepLot=minLot;

   lot=MathMax(minLot,MathMin(maxLot,lot));
   lot=MathFloor(lot/stepLot+1e-9)*stepLot;
   return NormalizeDouble(lot,VolumeDigits(stepLot));
  }

int VolumeDigits(double step)
  {
   int digits=0;
   while(digits<8 && MathAbs(step-MathRound(step))>1e-9)
     {
      step*=10.0;
      digits++;
     }
   return digits;
  }

//+------------------------------------------------------------------+
//| Pending order                                                    |
//+------------------------------------------------------------------+
bool PlacePendingOrder(ENUM_ORDER_TYPE type,double price,double lot,string comment)
  {
   MqlTradeRequest req={};
   MqlTradeResult res={};
   req.action=TRADE_ACTION_PENDING;
   req.symbol=Symbol();
   req.volume=lot;
   req.type=type;
   req.price=price;
   req.sl=0.0;
   req.tp=0.0;
   req.type_filling=ORDER_FILLING_RETURN;
   req.deviation=20;
   req.magic=BOT1_MAGIC;
   req.comment=comment;

   ResetLastError();
   bool sent=OrderSend(req,res);
   if(!sent || !TradeRetcodeAccepted(res.retcode))
     {
      Print("AMAR BOT1: pending failed [",comment,"] retcode=",res.retcode," lastError=",GetLastError());
      return false;
     }

   Print("AMAR BOT1: pending accepted [",comment,"] ticket=",res.order," price=",price," lot=",lot);
   return true;
  }

//+------------------------------------------------------------------+
//| Price tracking / market reinforcement                            |
//+------------------------------------------------------------------+
void TrackPrice()
  {
   int positions=Bot1PositionCount();
   if(positions>=MaxOrders) return;

   double bid=SymbolInfoDouble(Symbol(),SYMBOL_BID);
   double ask=SymbolInfoDouble(Symbol(),SYMBOL_ASK);
   double point=SymbolInfoDouble(Symbol(),SYMBOL_POINT);
   if(point<=0.0 || bid<=0.0 || ask<=0.0) return;
   double step=GridStep*point;

   int buyCount=0;
   int sellCount=0;
   for(int i=0;i<PositionsTotal();i++)
     {
      ulong ticket=PositionGetTicket(i);
      if(!IsBot1Position(ticket)) continue;
      ENUM_POSITION_TYPE type=(ENUM_POSITION_TYPE)PositionGetInteger(POSITION_TYPE);
      if(type==POSITION_TYPE_BUY) buyCount++;
      if(type==POSITION_TYPE_SELL) sellCount++;
     }

   if(BuyEnabled && buyCount<MaxOrders)
     {
      if(LastBuyPrice==0.0)
        {
         if(OpenMarketOrder(ORDER_TYPE_BUY,ask,GetLotSize(1)))
           {
            LastBuyPrice=bid;
            BuyStepCount=1;
           }
        }
      else if(LastBuyPrice-bid>=step)
        {
         int next=BuyStepCount+1;
         if(OpenMarketOrder(ORDER_TYPE_BUY,ask,GetLotSize(next)))
           {
            LastBuyPrice=bid;
            BuyStepCount=next;
           }
        }
     }

   if(SellEnabled && Bot1PositionCount()<MaxOrders && sellCount<MaxOrders)
     {
      if(LastSellPrice==0.0)
        {
         if(OpenMarketOrder(ORDER_TYPE_SELL,bid,GetLotSize(1)))
           {
            LastSellPrice=bid;
            SellStepCount=1;
           }
        }
      else if(bid-LastSellPrice>=step)
        {
         int next=SellStepCount+1;
         if(OpenMarketOrder(ORDER_TYPE_SELL,bid,GetLotSize(next)))
           {
            LastSellPrice=bid;
            SellStepCount=next;
           }
        }
     }
  }

bool OpenMarketOrder(ENUM_ORDER_TYPE type,double price,double lot)
  {
   MqlTradeRequest req={};
   MqlTradeResult res={};
   req.action=TRADE_ACTION_DEAL;
   req.symbol=Symbol();
   req.volume=lot;
   req.type=type;
   req.price=price;
   req.sl=0.0;
   req.tp=0.0;
   req.type_filling=ORDER_FILLING_IOC;
   req.deviation=30;
   req.magic=BOT1_MAGIC;
   req.comment=(type==ORDER_TYPE_BUY)?"BUY_GRID":"SELL_GRID";

   ResetLastError();
   bool sent=OrderSend(req,res);
   if(!sent || !TradeRetcodeAccepted(res.retcode))
     {
      Print("AMAR BOT1: market order failed retcode=",res.retcode," lastError=",GetLastError());
      return false;
     }

   Print("AMAR BOT1: market order accepted ticket=",res.deal," order=",res.order," lot=",lot);
   return true;
  }

//+------------------------------------------------------------------+
//| Basket                                                           |
//+------------------------------------------------------------------+
void CheckBasket()
  {
   double totalProfit=0.0;
   for(int i=0;i<PositionsTotal();i++)
     {
      ulong ticket=PositionGetTicket(i);
      if(!IsBot1Position(ticket)) continue;
      totalProfit+=PositionGetDouble(POSITION_PROFIT);
      totalProfit+=PositionGetDouble(POSITION_SWAP);
     }

   bool shouldClose=false;
   if(BasketTP>0.0 && totalProfit>=BasketTP)
      shouldClose=true;
   if(BasketSL>0.0 && totalProfit<=-BasketSL)
      shouldClose=true;

   if(!shouldClose) return;

   Print("AMAR BOT1: basket threshold reached: $",DoubleToString(totalProfit,2));
   CloseAll();
   ResetCounters();

   if(BotState==BOT_RUNNING)
      BuildGridOnly();
  }

//+------------------------------------------------------------------+
//| Trailing                                                         |
//+------------------------------------------------------------------+
void ManageTrailing()
  {
   if(Trail<=0) return;
   double point=SymbolInfoDouble(Symbol(),SYMBOL_POINT);
   if(point<=0.0) return;

   for(int i=PositionsTotal()-1;i>=0;i--)
     {
      ulong ticket=PositionGetTicket(i);
      if(!IsBot1Position(ticket)) continue;

      double sl=PositionGetDouble(POSITION_SL);
      double open=PositionGetDouble(POSITION_PRICE_OPEN);
      double cur=PositionGetDouble(POSITION_PRICE_CURRENT);
      ENUM_POSITION_TYPE type=(ENUM_POSITION_TYPE)PositionGetInteger(POSITION_TYPE);

      if(type==POSITION_TYPE_BUY && cur-open>Trail*point)
        {
         double newSl=NormalizePrice(cur-Trail*point);
         if(sl==0.0 || newSl>sl) ModifySL(ticket,newSl);
        }
      else if(type==POSITION_TYPE_SELL && open-cur>Trail*point)
        {
         double newSl=NormalizePrice(cur+Trail*point);
         if(sl==0.0 || newSl<sl) ModifySL(ticket,newSl);
        }
     }
  }

bool ModifySL(ulong ticket,double sl)
  {
   if(!PositionSelectByTicket(ticket)) return false;
   MqlTradeRequest req={};
   MqlTradeResult res={};
   req.action=TRADE_ACTION_SLTP;
   req.symbol=Symbol();
   req.position=ticket;
   req.sl=sl;
   req.tp=PositionGetDouble(POSITION_TP);

   ResetLastError();
   bool sent=OrderSend(req,res);
   if(!sent || !TradeRetcodeAccepted(res.retcode))
     {
      Print("AMAR BOT1: SL modification failed ticket=",ticket," retcode=",res.retcode," lastError=",GetLastError());
      return false;
     }
   return true;
  }

//+------------------------------------------------------------------+
//| Close/delete BOT1 scope only                                     |
//+------------------------------------------------------------------+
void DeletePending()
  {
   for(int i=OrdersTotal()-1;i>=0;i--)
     {
      ulong ticket=OrderGetTicket(i);
      if(!IsBot1Order(ticket)) continue;

      MqlTradeRequest req={};
      MqlTradeResult res={};
      req.action=TRADE_ACTION_REMOVE;
      req.order=ticket;

      ResetLastError();
      bool sent=OrderSend(req,res);
      if(!sent || !TradeRetcodeAccepted(res.retcode))
         Print("AMAR BOT1: pending delete failed ticket=",ticket," retcode=",res.retcode," lastError=",GetLastError());
     }
  }

void CloseAll()
  {
   BotState=BOT_CLOSING;
   UpdateUI();

   for(int i=PositionsTotal()-1;i>=0;i--)
     {
      ulong ticket=PositionGetTicket(i);
      if(!IsBot1Position(ticket)) continue;
      ClosePosition(ticket);
     }

   DeletePending();
   ResetCounters();

   if(BotState!=BOT_ERROR && BotState!=BOT_EMERGENCY_LOCK)
      BotState=BOT_OFF;
   UpdateUI();
  }

bool ClosePosition(ulong ticket)
  {
   if(!PositionSelectByTicket(ticket)) return false;

   ENUM_POSITION_TYPE posType=(ENUM_POSITION_TYPE)PositionGetInteger(POSITION_TYPE);
   double volume=PositionGetDouble(POSITION_VOLUME);
   double price=(posType==POSITION_TYPE_BUY)?SymbolInfoDouble(Symbol(),SYMBOL_BID):SymbolInfoDouble(Symbol(),SYMBOL_ASK);

   MqlTradeRequest req={};
   MqlTradeResult res={};
   req.action=TRADE_ACTION_DEAL;
   req.symbol=Symbol();
   req.position=ticket;
   req.type=(posType==POSITION_TYPE_BUY)?ORDER_TYPE_SELL:ORDER_TYPE_BUY;
   req.volume=volume;
   req.price=price;
   req.deviation=50;
   req.magic=BOT1_MAGIC;
   req.type_filling=ORDER_FILLING_IOC;

   ResetLastError();
   bool sent=OrderSend(req,res);
   if(!sent || !TradeRetcodeAccepted(res.retcode))
     {
      Print("AMAR BOT1: close failed ticket=",ticket," retcode=",res.retcode," lastError=",GetLastError());
      return false;
     }

   return true;
  }

bool TradeRetcodeAccepted(uint retcode)
  {
   return retcode==TRADE_RETCODE_DONE ||
          retcode==TRADE_RETCODE_DONE_PARTIAL ||
          retcode==TRADE_RETCODE_PLACED;
  }

//+------------------------------------------------------------------+
//| Counters / price helpers                                         |
//+------------------------------------------------------------------+
void ResetCounters()
  {
   LastBuyPrice=0.0;
   LastSellPrice=0.0;
   BuyStepCount=0;
   SellStepCount=0;
  }

double NormalizePrice(double price)
  {
   int digits=(int)SymbolInfoInteger(Symbol(),SYMBOL_DIGITS);
   return NormalizeDouble(price,digits);
  }

string StateText()
  {
   switch(BotState)
     {
      case BOT_OFF: return "OFF";
      case BOT_STARTING: return "STARTING";
      case BOT_RUNNING: return "RUNNING";
      case BOT_STOPPING: return "STOPPING";
      case BOT_REBUILDING: return "REBUILDING";
      case BOT_CLOSING: return "CLOSING";
      case BOT_ERROR: return "ERROR";
      case BOT_EMERGENCY_LOCK: return "EMERGENCY_LOCK";
     }
   return "UNKNOWN";
  }

//+------------------------------------------------------------------+
//| UI                                                               |
//+------------------------------------------------------------------+
void CreateUI()
  {
   ObjectsDeleteAll(0,PREFIX);
   int x=10,y=30,w=70;
   int corner=CORNER_RIGHT_UPPER;

   CreateLabel(PREFIX+"title","AMAR BOT 1 • GRID + MARTINGALE",x,y-25,clrGold,14,corner);
   CreateLabel(PREFIX+"state","STATE: OFF",x,y,clrWhite,11,corner); y+=28;

   CreateLabel(PREFIX+"lblLot","اللوت الابتدائي",x,y,clrWhite,10,corner);
   CreateEdit(PREFIX+"editLot",DoubleToString(LotStart,2),x+110,y,w,corner);
   CreateLabel(PREFIX+"lblStep","مسافة الشبكة",x+200,y,clrWhite,10,corner);
   CreateEdit(PREFIX+"editStep",IntegerToString(GridStep),x+290,y,w,corner);
   CreateLabel(PREFIX+"lblMax","الحد الأقصى",x+380,y,clrWhite,10,corner);
   CreateEdit(PREFIX+"editMax",IntegerToString(MaxOrders),x+460,y,w,corner); y+=30;

   CreateLabel(PREFIX+"lblMart","مضاعفة اللوت",x,y,clrWhite,10,corner);
   CreateEdit(PREFIX+"editMart",DoubleToString(Martingale,2),x+100,y,w,corner);
   CreateLabel(PREFIX+"lblTP","هدف السلة ($)",x+190,y,clrWhite,10,corner);
   CreateEdit(PREFIX+"editTP",DoubleToString(BasketTP,2),x+300,y,w,corner);
   CreateLabel(PREFIX+"lblSL","خسارة السلة ($)",x+390,y,clrWhite,10,corner);
   CreateEdit(PREFIX+"editSL",DoubleToString(BasketSL,2),x+500,y,w,corner); y+=30;

   CreateLabel(PREFIX+"lblTrail","Trailing (points)",x,y,clrWhite,10,corner);
   CreateEdit(PREFIX+"editTrail",IntegerToString(Trail),x+100,y,w,corner); y+=35;

   CreateButton(PREFIX+"btnBuy","شراء",x,y,60,28,BuyEnabled?clrLimeGreen:clrGray,corner);
   CreateButton(PREFIX+"btnSell","بيع",x+70,y,60,28,SellEnabled?clrCrimson:clrGray,corner);
   CreateButton(PREFIX+"btnToggle","▶ تشغيل",x+140,y,90,28,BotState==BOT_RUNNING?clrGreen:clrRed,corner); y+=35;

   CreateButton(PREFIX+"btnClose","🔴 إغلاق BOT1",x,y,110,35,clrDarkRed,corner);
   CreateButton(PREFIX+"btnRebuild","🔄 إعادة بناء",x+120,y,110,35,clrDodgerBlue,corner);

   ChartRedraw(0);
  }

void UpdateUI()
  {
   if(ObjectFind(0,PREFIX+"state")>=0)
      ObjectSetString(0,PREFIX+"state",OBJPROP_TEXT,"STATE: "+StateText()+" | POS="+IntegerToString(Bot1PositionCount())+" | PEND="+IntegerToString(Bot1PendingCount()));

   if(ObjectFind(0,PREFIX+"btnToggle")>=0)
     {
      ObjectSetString(0,PREFIX+"btnToggle",OBJPROP_TEXT,BotState==BOT_RUNNING?"⏹ إيقاف":"▶ تشغيل");
      ObjectSetInteger(0,PREFIX+"btnToggle",OBJPROP_BGCOLOR,BotState==BOT_RUNNING?clrGreen:clrRed);
     }

   if(ObjectFind(0,PREFIX+"btnBuy")>=0)
      ObjectSetInteger(0,PREFIX+"btnBuy",OBJPROP_BGCOLOR,BuyEnabled?clrLimeGreen:clrGray);
   if(ObjectFind(0,PREFIX+"btnSell")>=0)
      ObjectSetInteger(0,PREFIX+"btnSell",OBJPROP_BGCOLOR,SellEnabled?clrCrimson:clrGray);

   ChartRedraw(0);
  }

void CreateLabel(string n,string t,int x,int y,color c=clrWhite,int s=10,int corner=CORNER_RIGHT_UPPER)
  {
   ObjectCreate(0,n,OBJ_LABEL,0,0,0);
   ObjectSetInteger(0,n,OBJPROP_CORNER,corner);
   ObjectSetInteger(0,n,OBJPROP_XDISTANCE,x);
   ObjectSetInteger(0,n,OBJPROP_YDISTANCE,y);
   ObjectSetString(0,n,OBJPROP_TEXT,t);
   ObjectSetString(0,n,OBJPROP_FONT,"Arial");
   ObjectSetInteger(0,n,OBJPROP_FONTSIZE,s);
   ObjectSetInteger(0,n,OBJPROP_COLOR,c);
  }

void CreateEdit(string n,string t,int x,int y,int w=70,int corner=CORNER_RIGHT_UPPER)
  {
   ObjectCreate(0,n,OBJ_EDIT,0,0,0);
   ObjectSetInteger(0,n,OBJPROP_CORNER,corner);
   ObjectSetInteger(0,n,OBJPROP_XDISTANCE,x);
   ObjectSetInteger(0,n,OBJPROP_YDISTANCE,y);
   ObjectSetInteger(0,n,OBJPROP_XSIZE,w);
   ObjectSetInteger(0,n,OBJPROP_YSIZE,22);
   ObjectSetString(0,n,OBJPROP_TEXT,t);
   ObjectSetString(0,n,OBJPROP_FONT,"Arial");
   ObjectSetInteger(0,n,OBJPROP_FONTSIZE,10);
   ObjectSetInteger(0,n,OBJPROP_COLOR,clrBlack);
   ObjectSetInteger(0,n,OBJPROP_BGCOLOR,clrWhite);
   ObjectSetInteger(0,n,OBJPROP_ALIGN,ALIGN_CENTER);
  }

void CreateButton(string n,string t,int x,int y,int w=80,int h=28,color b=clrDimGray,int corner=CORNER_RIGHT_UPPER)
  {
   ObjectCreate(0,n,OBJ_BUTTON,0,0,0);
   ObjectSetInteger(0,n,OBJPROP_CORNER,corner);
   ObjectSetInteger(0,n,OBJPROP_XDISTANCE,x);
   ObjectSetInteger(0,n,OBJPROP_YDISTANCE,y);
   ObjectSetInteger(0,n,OBJPROP_XSIZE,w);
   ObjectSetInteger(0,n,OBJPROP_YSIZE,h);
   ObjectSetString(0,n,OBJPROP_TEXT,t);
   ObjectSetString(0,n,OBJPROP_FONT,"Arial");
   ObjectSetInteger(0,n,OBJPROP_FONTSIZE,10);
   ObjectSetInteger(0,n,OBJPROP_COLOR,clrWhite);
   ObjectSetInteger(0,n,OBJPROP_BGCOLOR,b);
   ObjectSetInteger(0,n,OBJPROP_BORDER_COLOR,clrBlack);
   ObjectSetInteger(0,n,OBJPROP_STATE,false);
  }
//+------------------------------------------------------------------+
