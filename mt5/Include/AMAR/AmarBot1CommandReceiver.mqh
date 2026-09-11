#property strict

// B37 BOT 1 terminal-side receiver. Commands are produced by the authenticated
// bridge and read from the MT5 FILE_COMMON sandbox. Expiry/request identity is
// checked again at the terminal so stale files cannot trigger execution.

#define AMAR_BOT1_COMMAND_FILE "AMAR_BOT1_COMMANDS.jsonl"
#define AMAR_BOT1_ACK_FILE "AMAR_BOT1_ACK.jsonl"

enum AMAR_BOT1_COMMAND
  {
   AMAR_CMD_NONE=0,
   AMAR_CMD_START,
   AMAR_CMD_STOP,
   AMAR_CMD_REBUILD,
   AMAR_CMD_CLOSE_ALL,
   AMAR_CMD_SET_BUY_ENABLED,
   AMAR_CMD_SET_SELL_ENABLED,
   AMAR_CMD_UPDATE_SETTINGS
  };

struct AmarBot1RemoteCommand
  {
   AMAR_BOT1_COMMAND type;
   bool hasEnabled;
   bool enabled;
   bool hasSettings;
   double lotStart;
   double gridStep;
   int maxOrders;
   double martingale;
   double basketTp;
   double basketSl;
   double trailing;
   bool hasBuyEnabled;
   bool buyEnabled;
   bool hasSellEnabled;
   bool sellEnabled;
   bool hasTargetSymbol;
   string targetSymbol;
   string requestId;
   string idempotencyKey;
   long expiresAtMs;
  };

class CAmarBot1CommandReceiver
  {
private:
   string m_lastRequestId;

   string ValueAfter(string src,string key)
     {
      int p=StringFind(src,"\""+key+"\"");
      if(p<0) return "";
      p=StringFind(src,":",p);
      if(p<0) return "";
      p++;
      while(p<StringLen(src) && (StringGetCharacter(src,p)==' ' || StringGetCharacter(src,p)=='\"')) p++;
      int e=p;
      while(e<StringLen(src))
        {
         ushort c=StringGetCharacter(src,e);
         if(c==',' || c=='}' || c=='\"') break;
         e++;
        }
      return StringSubstr(src,p,e-p);
     }

   bool ParseBool(string src,string key,bool &out)
     {
      string v=ValueAfter(src,key);
      if(v=="true") { out=true; return true; }
      if(v=="false") { out=false; return true; }
      return false;
     }

   bool ParseDouble(string src,string key,double &out)
     {
      string v=ValueAfter(src,key);
      if(v=="") return false;
      out=StringToDouble(v);
      return MathIsValidNumber(out);
     }

   bool ParseInt(string src,string key,int &out)
     {
      string v=ValueAfter(src,key);
      if(v=="") return false;
      out=(int)StringToInteger(v);
      return true;
     }

   long ParseLong(string src,string key)
     {
      string v=ValueAfter(src,key);
      if(v=="") return 0;
      return (long)StringToInteger(v);
     }

   bool HasTargetSymbol(string src)
     {
      return StringFind(src,"\"target_symbol\"")>=0;
     }

public:
   CAmarBot1CommandReceiver():m_lastRequestId("") {}

   bool Read(AmarBot1RemoteCommand &out)
     {
      ZeroMemory(out);
      int h=FileOpen(AMAR_BOT1_COMMAND_FILE,FILE_READ|FILE_TXT|FILE_COMMON|FILE_ANSI|FILE_SHARE_READ);
      if(h==INVALID_HANDLE) return false;
      string line="";
      while(!FileIsEnding(h))
        {
         string candidate=FileReadString(h);
         if(StringLen(candidate)>0) line=candidate;
        }
      FileClose(h);
      if(line=="") return false;

      out.requestId=ValueAfter(line,"request_id");
      out.idempotencyKey=ValueAfter(line,"idempotency_key");
      out.expiresAtMs=ParseLong(line,"expires_at_ms");
      if(out.requestId=="" || out.idempotencyKey=="" || out.expiresAtMs<=0) return false;
      if(out.requestId==m_lastRequestId) return false;

      long nowMs=(long)TimeCurrent()*1000;
      if(out.expiresAtMs<=nowMs)
        {
         m_lastRequestId=out.requestId;
         return false;
        }

      string cmd=ValueAfter(line,"command");
      if(cmd=="START") out.type=AMAR_CMD_START;
      else if(cmd=="STOP") out.type=AMAR_CMD_STOP;
      else if(cmd=="REBUILD") out.type=AMAR_CMD_REBUILD;
      else if(cmd=="CLOSE_ALL") out.type=AMAR_CMD_CLOSE_ALL;
      else if(cmd=="SET_BUY_ENABLED") out.type=AMAR_CMD_SET_BUY_ENABLED;
      else if(cmd=="SET_SELL_ENABLED") out.type=AMAR_CMD_SET_SELL_ENABLED;
      else if(cmd=="UPDATE_SETTINGS") out.type=AMAR_CMD_UPDATE_SETTINGS;
      else return false;

      if(HasTargetSymbol(line))
        {
         out.targetSymbol=ValueAfter(line,"target_symbol");
         out.hasTargetSymbol=(StringLen(out.targetSymbol)>0);
         if(!out.hasTargetSymbol) return false;
        }

      if(out.type==AMAR_CMD_SET_BUY_ENABLED || out.type==AMAR_CMD_SET_SELL_ENABLED)
        {
         out.hasEnabled=ParseBool(line,"enabled",out.enabled);
         if(!out.hasEnabled) return false;
        }
      if(out.type==AMAR_CMD_UPDATE_SETTINGS)
        {
         out.hasSettings=true;
         if(!ParseDouble(line,"lot_start",out.lotStart) ||
            !ParseDouble(line,"grid_step",out.gridStep) ||
            !ParseInt(line,"max_orders",out.maxOrders) ||
            !ParseDouble(line,"martingale",out.martingale) ||
            !ParseDouble(line,"basket_tp",out.basketTp) ||
            !ParseDouble(line,"basket_sl",out.basketSl) ||
            !ParseDouble(line,"trailing",out.trailing) ||
            out.lotStart<=0 || out.gridStep<=0 || out.maxOrders<=0 || out.martingale<=0 || out.trailing<0)
           return false;
         out.hasBuyEnabled=ParseBool(line,"buy_enabled",out.buyEnabled);
         out.hasSellEnabled=ParseBool(line,"sell_enabled",out.sellEnabled);
         if(!out.hasBuyEnabled || !out.hasSellEnabled) return false;
        }
      m_lastRequestId=out.requestId;
      return true;
     }

   void Ack(string requestId,bool accepted,string message)
     {
      int h=FileOpen(AMAR_BOT1_ACK_FILE,FILE_WRITE|FILE_TXT|FILE_COMMON|FILE_ANSI|FILE_SHARE_READ);
      if(h==INVALID_HANDLE) return;
      string safe=message;
      StringReplace(safe,"\"","'");
      StringReplace(safe,"\r"," ");
      StringReplace(safe,"\n"," ");
      string payload=StringFormat("{\"request_id\":\"%s\",\"accepted\":%s,\"timestamp_ms\":%I64d,\"message\":\"%s\"}",requestId,accepted?"true":"false",(long)TimeCurrent()*1000,safe);
      FileWriteString(h,payload+"\n");
      FileFlush(h);
      FileClose(h);
     }
  };
