#property strict

// B39/B42/B47 terminal-side receiver. The bridge authenticates the command before it
// reaches FILE_COMMON; MT5 verifies expiry/shape/sequence again and reports ACK.

#define AMAR_BOT1_COMMAND_FILE "AMAR_BOT1_COMMANDS.jsonl"
#define AMAR_BOT1_ACK_FILE "AMAR_BOT1_ACK.jsonl"
#define AMAR_BOT1_ACK_SCAN_LIMIT 256
#define AMAR_BOT1_SEQUENCE_PREFIX "AMAR_BOT1_SEQ_"

enum AMAR_BOT1_COMMAND
  {
   AMAR_CMD_NONE=0,
   AMAR_CMD_START,
   AMAR_CMD_STOP,
   AMAR_CMD_REBUILD,
   AMAR_CMD_CLOSE_ALL,
   AMAR_CMD_CLOSE_BUY,
   AMAR_CMD_CLOSE_SELL,
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
   string deviceId;
   long sequence;
   long expiresAtMs;
  };

class CAmarBot1CommandReceiver
  {
private:
   string m_lastRequestId;
   string m_lastDeviceId;
   long m_lastSequence;

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

   string SequenceVariableName(string deviceId)
     {
      string compact=deviceId;
      StringReplace(compact,"-","");
      if(StringLen(compact)>48) compact=StringSubstr(compact,0,48);
      return AMAR_BOT1_SEQUENCE_PREFIX+compact;
     }

   long PersistedSequence(string deviceId)
     {
      if(deviceId=="") return 0;
      string name=SequenceVariableName(deviceId);
      if(!GlobalVariableCheck(name)) return 0;
      double value=0.0;
      if(!GlobalVariableGet(name,value)) return 0;
      if(!MathIsValidNumber(value) || value<0.0) return 0;
      return (long)value;
     }

   bool PersistSequence(string deviceId,long sequence)
     {
      if(deviceId=="" || sequence<=0) return false;
      return GlobalVariableSet(SequenceVariableName(deviceId),(double)sequence)>0;
     }

   bool HasProcessedRequest(string requestId)
     {
      if(requestId=="" || !FileIsExist(AMAR_BOT1_ACK_FILE,FILE_COMMON)) return false;
      int h=FileOpen(AMAR_BOT1_ACK_FILE,FILE_READ|FILE_TXT|FILE_COMMON|FILE_ANSI|FILE_SHARE_READ|FILE_SHARE_WRITE);
      if(h==INVALID_HANDLE) return false;
      int scanned=0;
      while(!FileIsEnding(h) && scanned<AMAR_BOT1_ACK_SCAN_LIMIT)
        {
         string line=FileReadString(h);
         if(StringLen(line)>0)
           {
            scanned++;
            if(ValueAfter(line,"request_id")==requestId)
              {
               FileClose(h);
               return true;
              }
           }
        }
      FileClose(h);
      return false;
     }

   bool Reject(string requestId,string reason)
     {
      if(requestId!="")
        {
         m_lastRequestId=requestId;
         Ack(requestId,false,reason);
        }
      return false;
     }

public:
   CAmarBot1CommandReceiver():m_lastRequestId(""),m_lastDeviceId(""),m_lastSequence(0) {}

   bool Read(AmarBot1RemoteCommand &out)
     {
      ZeroMemory(out);
      int h=FileOpen(AMAR_BOT1_COMMAND_FILE,FILE_READ|FILE_TXT|FILE_COMMON|FILE_ANSI|FILE_SHARE_READ|FILE_SHARE_WRITE);
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
      out.deviceId=ValueAfter(line,"device_id");
      out.sequence=ParseLong(line,"sequence");
      out.expiresAtMs=ParseLong(line,"expires_at_ms");
      if(out.requestId=="" || out.idempotencyKey=="" || out.expiresAtMs<=0) return false;
      if(out.requestId==m_lastRequestId) return false;
      if(HasProcessedRequest(out.requestId)) return false;

      long nowMs=(long)TimeCurrent()*1000;
      if(out.expiresAtMs<=nowMs) return Reject(out.requestId,"EXPIRED");
      if(out.deviceId=="" || out.sequence<=0) return Reject(out.requestId,"INVALID_SEQUENCE");

      long persisted=PersistedSequence(out.deviceId);
      long floor=MathMax(m_lastDeviceId==out.deviceId ? m_lastSequence : 0,persisted);
      if(out.sequence<=floor) return Reject(out.requestId,"OUT_OF_ORDER_SEQUENCE");

      string cmd=ValueAfter(line,"command");
      if(cmd=="START") out.type=AMAR_CMD_START;
      else if(cmd=="STOP") out.type=AMAR_CMD_STOP;
      else if(cmd=="REBUILD") out.type=AMAR_CMD_REBUILD;
      else if(cmd=="CLOSE_ALL") out.type=AMAR_CMD_CLOSE_ALL;
      else if(cmd=="CLOSE_BUY") out.type=AMAR_CMD_CLOSE_BUY;
      else if(cmd=="CLOSE_SELL") out.type=AMAR_CMD_CLOSE_SELL;
      else if(cmd=="SET_BUY_ENABLED") out.type=AMAR_CMD_SET_BUY_ENABLED;
      else if(cmd=="SET_SELL_ENABLED") out.type=AMAR_CMD_SET_SELL_ENABLED;
      else if(cmd=="UPDATE_SETTINGS") out.type=AMAR_CMD_UPDATE_SETTINGS;
      else return Reject(out.requestId,"UNSUPPORTED_COMMAND");

      if(HasTargetSymbol(line))
        {
         out.targetSymbol=ValueAfter(line,"target_symbol");
         out.hasTargetSymbol=(StringLen(out.targetSymbol)>0);
         if(!out.hasTargetSymbol) return Reject(out.requestId,"INVALID_TARGET_SYMBOL");
        }

      if(out.type==AMAR_CMD_SET_BUY_ENABLED || out.type==AMAR_CMD_SET_SELL_ENABLED)
        {
         out.hasEnabled=ParseBool(line,"enabled",out.enabled);
         if(!out.hasEnabled) return Reject(out.requestId,"INVALID_ENABLED_VALUE");
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
            out.lotStart<=0 || out.gridStep<=0 || out.maxOrders<=0 || out.martingale<=0 || out.trailing<0 ||
            !MathIsValidNumber(out.basketTp) || !MathIsValidNumber(out.basketSl))
           return Reject(out.requestId,"INVALID_SETTINGS");
         out.hasBuyEnabled=ParseBool(line,"buy_enabled",out.buyEnabled);
         out.hasSellEnabled=ParseBool(line,"sell_enabled",out.sellEnabled);
         if(!out.hasBuyEnabled || !out.hasSellEnabled) return Reject(out.requestId,"INVALID_SIDE_SETTINGS");
        }

      if(!PersistSequence(out.deviceId,out.sequence))
        return Reject(out.requestId,"SEQUENCE_PERSISTENCE_FAILED");
      m_lastRequestId=out.requestId;
      m_lastDeviceId=out.deviceId;
      m_lastSequence=out.sequence;
      return true;
     }

   void Ack(string requestId,bool accepted,string message)
     {
      int h=FileOpen(AMAR_BOT1_ACK_FILE,FILE_READ|FILE_WRITE|FILE_TXT|FILE_COMMON|FILE_ANSI|FILE_SHARE_READ|FILE_SHARE_WRITE);
      if(h==INVALID_HANDLE) return;
      FileSeek(h,0,SEEK_END);
      string safe=message;
      StringReplace(safe,"\"","'");
      StringReplace(safe,"\r"," ");
      StringReplace(safe,"\n"," ");
      string status=accepted ? "VERIFIED" : "FAILED";
      string payload=StringFormat("{\"request_id\":\"%s\",\"accepted\":%s,\"status\":\"%s\",\"timestamp_ms\":%I64d,\"message\":\"%s\"}",requestId,accepted?"true":"false",status,(long)TimeCurrent()*1000,safe);
      FileWriteString(h,payload+"\n");
      FileFlush(h);
      FileClose(h);
     }
  };