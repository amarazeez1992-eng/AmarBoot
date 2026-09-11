//+------------------------------------------------------------------+
//|                                     Grid_Martingale_Basket_v2.mq5|
//|       شبكة متقدمة - مضاعفة + سلة (ربح/خسارة) + إعادة بناء تلقائي|
//+------------------------------------------------------------------+
#property copyright "AMAR"
#property version   "2.00"
#property strict

//+------------------------------------------------------------------+
//| 1. المدخلات (تظهر عند التركيب)                                   |
//+------------------------------------------------------------------+
input double  InpLotStart     = 0.01;          // حجم العقد الابتدائي
input int    InpGridStep     = 30;             // مسافة الشبكة (نقطة)
input int    InpMaxOrders    = 10;             // الحد الأقصى للصفقات الإجمالية
input double InpMartingale   = 2.0;            // مضاعفة اللوت (مثل 2.0)
input double InpBasketTP     = 50.0;           // هدف الربح الكلي ($)
input double InpBasketSL     = -30.0;          // حد الخسارة الكلية ($)
input int    InpTrail        = 0;              // ستوب متحرك (0=إلغاء)
input bool   InpEnableBuy    = true;           // تفعيل الشراء
input bool   InpEnableSell   = true;           // تفعيل البيع

//+------------------------------------------------------------------+
//| 2. المتغيرات العامة                                              |
//+------------------------------------------------------------------+
string   Prefix = "GBM2_";
bool     IsTrading   = true;
bool     BuyEnabled  = true;
bool     SellEnabled = true;

double   LotStart     = 0.01;
int      GridStep     = 30;
int      MaxOrders    = 10;
double   Martingale   = 2.0;
double   BasketTP     = 50.0;
double   BasketSL     = -30.0;
int      Trail        = 0;
int      Magic        = 20260908;

double   LastBuyPrice  = 0;
double   LastSellPrice = 0;
int      BuyStepCount  = 0;
int      SellStepCount = 0;

//+------------------------------------------------------------------+
//| 3. بداية البوت                                                   |
//+------------------------------------------------------------------+
int OnInit()
{
   LotStart     = InpLotStart;
   GridStep     = InpGridStep;
   MaxOrders    = InpMaxOrders;
   Martingale   = InpMartingale;
   BasketTP     = InpBasketTP;
   BasketSL     = InpBasketSL;
   Trail        = InpTrail;
   BuyEnabled   = InpEnableBuy;
   SellEnabled  = InpEnableSell;

   CreateUI();
   if(IsTrading) BuildGrid();

   Print("✅ بوت السلة المتقدم يعمل - هدف الربح: $", BasketTP, " | حد الخسارة: $", BasketSL);
   return(INIT_SUCCEEDED);
}

void OnDeinit(const int reason)
{
   ObjectsDeleteAll(0, Prefix);
   Comment("");
}

//+------------------------------------------------------------------+
//| 4. التيك – المراقبة وإدارة الصفقات                             |
//+------------------------------------------------------------------+
void OnTick()
{
   if(!IsTrading) return;

   CheckBasket();

   if(Trail > 0) ManageTrailing();

   TrackPrice();
}

//+------------------------------------------------------------------+
//| 5. أحداث الواجهة (النقر والتعديل)                               |
//+------------------------------------------------------------------+
void OnChartEvent(const int id, const long &lparam, const double &dparam, const string &sparam)
{
   if(id == CHARTEVENT_OBJECT_CLICK)
   {
      if(sparam == Prefix+"btnClose")   { CloseAll(); ResetCounters(); return; }
      if(sparam == Prefix+"btnRebuild") { if(IsTrading) { CloseAll(); ResetCounters(); BuildGrid(); } return; }
      if(sparam == Prefix+"btnToggle")
      {
         IsTrading = !IsTrading;
         ObjectSetInteger(0, Prefix+"btnToggle", OBJPROP_BGCOLOR, IsTrading ? clrGreen : clrRed);
         ObjectSetString(0, Prefix+"btnToggle", OBJPROP_TEXT, IsTrading ? "► تشغيل" : "⏹ إيقاف");
         if(IsTrading) { CloseAll(); ResetCounters(); BuildGrid(); } else DeletePending();
         return;
      }
      if(sparam == Prefix+"btnBuy")
      {
         BuyEnabled = !BuyEnabled;
         ObjectSetInteger(0, Prefix+"btnBuy", OBJPROP_BGCOLOR, BuyEnabled ? clrLimeGreen : clrGray);
         if(IsTrading) { CloseAll(); ResetCounters(); BuildGrid(); }
         return;
      }
      if(sparam == Prefix+"btnSell")
      {
         SellEnabled = !SellEnabled;
         ObjectSetInteger(0, Prefix+"btnSell", OBJPROP_BGCOLOR, SellEnabled ? clrCrimson : clrGray);
         if(IsTrading) { CloseAll(); ResetCounters(); BuildGrid(); }
         return;
      }
   }

   if(id == CHARTEVENT_OBJECT_ENDEDIT)
   {
      string val = ObjectGetString(0, sparam, OBJPROP_TEXT);
      bool rebuild = false;
      if(sparam == Prefix+"editLot")    { LotStart = StringToDouble(val); rebuild = true; }
      else if(sparam == Prefix+"editStep") { GridStep = (int)StringToInteger(val); rebuild = true; }
      else if(sparam == Prefix+"editMax") { MaxOrders = (int)StringToInteger(val); rebuild = true; }
      else if(sparam == Prefix+"editMart") { Martingale = StringToDouble(val); rebuild = true; }
      else if(sparam == Prefix+"editTP")  { BasketTP = StringToDouble(val); }
      else if(sparam == Prefix+"editSL")  { BasketSL = StringToDouble(val); }
      else if(sparam == Prefix+"editTrail") { Trail = (int)StringToInteger(val); }

      if(rebuild && IsTrading) { CloseAll(); ResetCounters(); BuildGrid(); }
      ChartRedraw(0);
   }
}

//+------------------------------------------------------------------+
//| 6. الواجهة الرسومية (في الركن العلوي الأيمن)                    |
//+------------------------------------------------------------------+
void CreateUI()
{
   ObjectsDeleteAll(0, Prefix);
   int x = 10, y = 30, w = 70;
   int corner = CORNER_RIGHT_UPPER;

   CreateLabel(Prefix+"title", "🧠 شبكة + مضاعفة + سلة", x, y-25, clrGold, 14, corner); y+=5;

   CreateLabel(Prefix+"lblLot", "اللوت الابتدائي", x, y, clrWhite, 10, corner);
   CreateEdit(Prefix+"editLot", DoubleToString(LotStart,2), x+110, y, w, corner);
   CreateLabel(Prefix+"lblStep", "مسافة الشبكة", x+200, y, clrWhite, 10, corner);
   CreateEdit(Prefix+"editStep", IntegerToString(GridStep), x+290, y, w, corner);
   CreateLabel(Prefix+"lblMax", "الحد الأقصى", x+380, y, clrWhite, 10, corner);
   CreateEdit(Prefix+"editMax", IntegerToString(MaxOrders), x+460, y, w, corner); y+=30;

   CreateLabel(Prefix+"lblMart", "مضاعفة اللوت", x, y, clrWhite, 10, corner);
   CreateEdit(Prefix+"editMart", DoubleToString(Martingale,2), x+100, y, w, corner);
   CreateLabel(Prefix+"lblTP", "هدف الربح الكلي ($)", x+190, y, clrWhite, 10, corner);
   CreateEdit(Prefix+"editTP", DoubleToString(BasketTP,2), x+300, y, w, corner);
   CreateLabel(Prefix+"lblSL", "حد الخسارة الكلية ($)", x+390, y, clrWhite, 10, corner);
   CreateEdit(Prefix+"editSL", DoubleToString(BasketSL,2), x+500, y, w, corner); y+=30;

   CreateLabel(Prefix+"lblTrail", "ستوب متحرك", x, y, clrWhite, 10, corner);
   CreateEdit(Prefix+"editTrail", IntegerToString(Trail), x+100, y, w, corner); y+=35;

   CreateButton(Prefix+"btnBuy", "شراء", x, y, 60,28, BuyEnabled?clrLimeGreen:clrGray, corner);
   CreateButton(Prefix+"btnSell", "بيع", x+70, y, 60,28, SellEnabled?clrCrimson:clrGray, corner);
   CreateButton(Prefix+"btnToggle", IsTrading?"► تشغيل":"⏹ إيقاف", x+140, y, 80,28, IsTrading?clrGreen:clrRed, corner); y+=35;

   CreateButton(Prefix+"btnClose", "🔴 إغلاق الكل", x, y, 100,35, clrDarkRed, corner);
   CreateButton(Prefix+"btnRebuild", "🔄 إعادة بناء", x+110, y, 100,35, clrDodgerBlue, corner);

   ChartRedraw(0);
}

//--- دوال مساعدة مع تحديد الركن
void CreateLabel(string n,string t,int x,int y,color c=clrWhite,int s=10,int corner=CORNER_RIGHT_UPPER)
{
   ObjectCreate(0,n,OBJ_LABEL,0,0,0);
   ObjectSetInteger(0,n,OBJPROP_CORNER,corner);
   ObjectSetInteger(0,n,OBJPROP_XDISTANCE,x); ObjectSetInteger(0,n,OBJPROP_YDISTANCE,y);
   ObjectSetString(0,n,OBJPROP_TEXT,t);
   ObjectSetString(0,n,OBJPROP_FONT,"Arial"); ObjectSetInteger(0,n,OBJPROP_FONTSIZE,s);
   ObjectSetInteger(0,n,OBJPROP_COLOR,c);
}
void CreateEdit(string n,string t,int x,int y,int w=70,int corner=CORNER_RIGHT_UPPER)
{
   ObjectCreate(0,n,OBJ_EDIT,0,0,0);
   ObjectSetInteger(0,n,OBJPROP_CORNER,corner);
   ObjectSetInteger(0,n,OBJPROP_XDISTANCE,x); ObjectSetInteger(0,n,OBJPROP_YDISTANCE,y);
   ObjectSetInteger(0,n,OBJPROP_XSIZE,w);
   ObjectSetInteger(0,n,OBJPROP_YSIZE,22);
   ObjectSetString(0,n,OBJPROP_TEXT,t);
   ObjectSetString(0,n,OBJPROP_FONT,"Arial"); ObjectSetInteger(0,n,OBJPROP_FONTSIZE,10);
   ObjectSetInteger(0,n,OBJPROP_COLOR,clrBlack); ObjectSetInteger(0,n,OBJPROP_BGCOLOR,clrWhite);
   ObjectSetInteger(0,n,OBJPROP_ALIGN,ALIGN_CENTER);
}
void CreateButton(string n,string t,int x,int y,int w=80,int h=28,color b=clrDimGray,int corner=CORNER_RIGHT_UPPER)
{
   ObjectCreate(0,n,OBJ_BUTTON,0,0,0);
   ObjectSetInteger(0,n,OBJPROP_CORNER,corner);
   ObjectSetInteger(0,n,OBJPROP_XDISTANCE,x); ObjectSetInteger(0,n,OBJPROP_YDISTANCE,y);
   ObjectSetInteger(0,n,OBJPROP_XSIZE,w);
   ObjectSetInteger(0,n,OBJPROP_YSIZE,h);
   ObjectSetString(0,n,OBJPROP_TEXT,t);
   ObjectSetString(0,n,OBJPROP_FONT,"Arial"); ObjectSetInteger(0,n,OBJPROP_FONTSIZE,10);
   ObjectSetInteger(0,n,OBJPROP_COLOR,clrWhite); ObjectSetInteger(0,n,OBJPROP_BGCOLOR,b);
   ObjectSetInteger(0,n,OBJPROP_BORDER_COLOR,clrBlack); ObjectSetInteger(0,n,OBJPROP_STATE,false);
}

//+------------------------------------------------------------------+
//| 7. بناء الشبكة المبدئية                                          |
//+------------------------------------------------------------------+
void BuildGrid()
{
   if(!IsTrading) return;
   DeletePending();
   CloseAll();

   double point = SymbolInfoDouble(Symbol(), SYMBOL_POINT);
   double bid = SymbolInfoDouble(Symbol(), SYMBOL_BID);
   double ask = SymbolInfoDouble(Symbol(), SYMBOL_ASK);
   double step = GridStep * point;

   if(BuyEnabled)
   {
      for(int i=1; i<=MaxOrders/2; i++)
      {
         double price = ask + (i * step);
         double lot = GetLotSize(ORDER_TYPE_BUY_STOP, i);
         PlacePendingOrder(ORDER_TYPE_BUY_STOP, price, lot, "BUY_STOP_"+IntegerToString(i));
      }
   }

   if(SellEnabled)
   {
      for(int i=1; i<=MaxOrders/2; i++)
      {
         double price = bid - (i * step);
         double lot = GetLotSize(ORDER_TYPE_SELL_STOP, i);
         PlacePendingOrder(ORDER_TYPE_SELL_STOP, price, lot, "SELL_STOP_"+IntegerToString(i));
      }
   }

   ResetCounters();
   Print("✅ الشبكة المبدئية مبنية بـ ", MaxOrders, " أمر");
}

//+------------------------------------------------------------------+
//| 8. حساب حجم اللوت حسب مضاعفة مارتينجال                          |
//+------------------------------------------------------------------+
double GetLotSize(ENUM_ORDER_TYPE type, int stepNumber)
{
   double lot = LotStart * MathPow(Martingale, stepNumber-1);
   lot = MathRound(lot * 100) / 100.0;
   if(lot < 0.01) lot = 0.01;
   if(lot > 100) lot = 100;
   return lot;
}

//+------------------------------------------------------------------+
//| 9. وضع أمر معلق (Stop) مع إلغاء الـ SL/TP الفردي                |
//+------------------------------------------------------------------+
void PlacePendingOrder(ENUM_ORDER_TYPE type, double price, double lot, string cmt)
{
   MqlTradeRequest req = {};
   MqlTradeResult res = {};
   req.action = TRADE_ACTION_PENDING;
   req.symbol = Symbol();
   req.volume = lot;
   req.type = type;
   req.price = price;
   req.sl = 0;
   req.tp = 0;
   req.type_filling = ORDER_FILLING_RETURN;
   req.deviation = 20;
   req.magic = Magic;
   req.comment = cmt;

   if(!OrderSend(req, res))
      Print("❌ فشل [", cmt, "] الكود: ", res.retcode);
   else
      Print("✅ تم [", cmt, "] عند ", price, " بحجم ", lot);
}

//+------------------------------------------------------------------+
//| 10. تتبع السعر وفتح صفقات تعزيز جديدة                           |
//+------------------------------------------------------------------+
void TrackPrice()
{
   int total = PositionsTotal();
   if(total >= MaxOrders) return;

   double bid = SymbolInfoDouble(Symbol(), SYMBOL_BID);
   double ask = SymbolInfoDouble(Symbol(), SYMBOL_ASK);
   double point = SymbolInfoDouble(Symbol(), SYMBOL_POINT);
   double step = GridStep * point;

   int buyCount=0, sellCount=0;
   for(int i=0; i<total; i++)
   {
      ulong ticket = PositionGetTicket(i);
      if(PositionSelectByTicket(ticket))
      {
         if(PositionGetInteger(POSITION_TYPE) == POSITION_TYPE_BUY) buyCount++;
         else if(PositionGetInteger(POSITION_TYPE) == POSITION_TYPE_SELL) sellCount++;
      }
   }

   if(BuyEnabled && buyCount < MaxOrders)
   {
      if(LastBuyPrice == 0)
      {
         double lot = GetLotSize(ORDER_TYPE_BUY, 1);
         if(OpenMarketOrder(ORDER_TYPE_BUY, ask, lot))
         {
            LastBuyPrice = bid;
            BuyStepCount = 1;
         }
      }
      else if(LastBuyPrice - bid >= step)
      {
         BuyStepCount++;
         double lot = GetLotSize(ORDER_TYPE_BUY, BuyStepCount);
         if(OpenMarketOrder(ORDER_TYPE_BUY, ask, lot))
            LastBuyPrice = bid;
      }
   }

   if(SellEnabled && sellCount < MaxOrders)
   {
      if(LastSellPrice == 0)
      {
         double lot = GetLotSize(ORDER_TYPE_SELL, 1);
         if(OpenMarketOrder(ORDER_TYPE_SELL, bid, lot))
         {
            LastSellPrice = bid;
            SellStepCount = 1;
         }
      }
      else if(bid - LastSellPrice >= step)
      {
         SellStepCount++;
         double lot = GetLotSize(ORDER_TYPE_SELL, SellStepCount);
         if(OpenMarketOrder(ORDER_TYPE_SELL, bid, lot))
            LastSellPrice = bid;
      }
   }
}

//+------------------------------------------------------------------+
//| 11. فتح صفقة سوق (Market) بدون SL/TP فردي                       |
//+------------------------------------------------------------------+
bool OpenMarketOrder(ENUM_ORDER_TYPE type, double price, double lot)
{
   MqlTradeRequest req = {};
   MqlTradeResult res = {};
   req.action = TRADE_ACTION_DEAL;
   req.symbol = Symbol();
   req.volume = lot;
   req.type = type;
   req.price = price;
   req.sl = 0;
   req.tp = 0;
   req.type_filling = ORDER_FILLING_IOC;
   req.deviation = 30;
   req.magic = Magic;
   req.comment = (type==ORDER_TYPE_BUY) ? "BUY_GRID" : "SELL_GRID";

   if(!OrderSend(req, res))
   {
      Print("❌ فشل فتح ", (type==ORDER_TYPE_BUY?"شراء":"بيع"), " - الكود: ", res.retcode);
      return false;
   }
   else
   {
      Print("✅ فتح ", (type==ORDER_TYPE_BUY?"شراء":"بيع"), " عند ", price, " بحجم ", lot);
      return true;
   }
}

//+------------------------------------------------------------------+
//| 12. مراقبة السلة (ربح/خسارة) وإعادة البناء تلقائياً            |
//+------------------------------------------------------------------+
void CheckBasket()
{
   double totalProfit = 0;
   int total = PositionsTotal();
   for(int i=0; i<total; i++)
   {
      ulong ticket = PositionGetTicket(i);
      if(PositionSelectByTicket(ticket))
         totalProfit += PositionGetDouble(POSITION_PROFIT);
   }

   bool shouldClose = false;

   if(totalProfit >= BasketTP && BasketTP > 0)
   {
      Print("💰 ✅ تحقيق الربح الكلي: $", DoubleToString(totalProfit, 2));
      shouldClose = true;
   }

   if(totalProfit <= BasketSL && BasketSL < 0)
   {
      Print("💸 ❌ تحقيق الخسارة الكلية: $", DoubleToString(totalProfit, 2));
      shouldClose = true;
   }

   if(shouldClose)
   {
      CloseAll();
      ResetCounters();
      if(IsTrading)
      {
         Print("🔄 إعادة بناء الشبكة تلقائياً...");
         BuildGrid();
      }
   }
}

//+------------------------------------------------------------------+
//| 13. الستوب المتحرك                                               |
//+------------------------------------------------------------------+
void ManageTrailing()
{
   if(Trail <= 0) return;
   for(int i=PositionsTotal()-1; i>=0; i--)
   {
      ulong ticket = PositionGetTicket(i);
      if(!PositionSelectByTicket(ticket)) continue;

      double sl = PositionGetDouble(POSITION_SL);
      double open = PositionGetDouble(POSITION_PRICE_OPEN);
      double cur = PositionGetDouble(POSITION_PRICE_CURRENT);
      ENUM_POSITION_TYPE type = (ENUM_POSITION_TYPE)PositionGetInteger(POSITION_TYPE);
      double point = SymbolInfoDouble(Symbol(), SYMBOL_POINT);

      if(type == POSITION_TYPE_BUY && cur - open > Trail * point && (sl == 0 || cur - sl > Trail * point))
         ModifySL(ticket, cur - Trail * point);
      else if(type == POSITION_TYPE_SELL && open - cur > Trail * point && (sl == 0 || sl - cur > Trail * point))
         ModifySL(ticket, cur + Trail * point);
   }
}
void ModifySL(ulong ticket, double sl)
{
   MqlTradeRequest req = {};
   MqlTradeResult res = {};
   req.action = TRADE_ACTION_SLTP;
   req.symbol = Symbol();
   req.position = ticket;
   req.sl = sl;
   OrderSend(req, res);
}

//+------------------------------------------------------------------+
//| 14. حذف الأوامر المعلقة                                          |
//+------------------------------------------------------------------+
void DeletePending()
{
   int total = OrdersTotal();
   int deleted = 0;
   for(int i=total-1; i>=0; i--)
   {
      ulong ticket = OrderGetTicket(i);
      if(OrderSelect(ticket) && OrderGetInteger(ORDER_MAGIC)==Magic && OrderGetString(ORDER_SYMBOL)==Symbol())
      {
         MqlTradeRequest req = {};
         MqlTradeResult res = {};
         req.action = TRADE_ACTION_REMOVE;
         req.order = ticket;
         if(OrderSend(req, res)) deleted++;
      }
   }
   if(deleted > 0) Print("🗑️ تم حذف ", deleted, " أمر معلق");
}

//+------------------------------------------------------------------+
//| 15. إعادة ضبط العدادات                                           |
//+------------------------------------------------------------------+
void ResetCounters()
{
   LastBuyPrice = 0;
   LastSellPrice = 0;
   BuyStepCount = 0;
   SellStepCount = 0;
}

//+------------------------------------------------------------------+
//| 16. إغلاق جميع الصفقات                                           |
//+------------------------------------------------------------------+
void CloseAll()
{
   int closed = 0;
   for(int i=PositionsTotal()-1; i>=0; i--)
   {
      ulong ticket = PositionGetTicket(i);
      if(!PositionSelectByTicket(ticket)) continue;

      MqlTradeRequest req = {};
      MqlTradeResult res = {};
      req.action = TRADE_ACTION_DEAL;
      req.symbol = Symbol();
      req.position = ticket;
      req.type = (PositionGetInteger(POSITION_TYPE) == POSITION_TYPE_BUY) ? ORDER_TYPE_SELL : ORDER_TYPE_BUY;
      req.volume = PositionGetDouble(POSITION_VOLUME);
      req.price = (req.type == ORDER_TYPE_BUY) ? SymbolInfoDouble(Symbol(), SYMBOL_ASK) : SymbolInfoDouble(Symbol(), SYMBOL_BID);
      req.deviation = 50;
      req.magic = Magic;

      if(OrderSend(req, res)) closed++;
   }
   if(closed > 0) Print("🔒 تم إغلاق ", closed, " صفقة");
}
//+------------------------------------------------------------------+
