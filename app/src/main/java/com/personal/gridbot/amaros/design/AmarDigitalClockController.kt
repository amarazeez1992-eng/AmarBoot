package com.personal.gridbot.amaros.design

/** Independent compact premium digital clock controller for the home WebView. */
object AmarDigitalClockController {
    fun script(): String = """
        (function(){
          var c=document.getElementById('clock');
          if(!c)return;
          if(window.__amarDigitalClockTimer)clearTimeout(window.__amarDigitalClockTimer);
          c.className='amar-digital-clock';
          c.innerHTML='<span class="adc-time">00:00:00</span><span class="adc-date">--/--/----</span>';
          c.style.cssText='position:relative;width:100px;height:54px;border-radius:14px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:1px;box-sizing:border-box;overflow:hidden;direction:ltr;background:linear-gradient(135deg,rgba(4,4,8,.98),rgba(24,10,42,.96) 42%,rgba(17,12,45,.96) 68%,rgba(8,5,18,.98));border:1px solid rgba(241,196,76,.92);box-shadow:0 0 0 1px rgba(236,72,153,.34),0 0 0 3px rgba(92,55,180,.16),0 0 18px rgba(241,196,76,.25),0 0 30px rgba(168,85,247,.22),0 0 34px rgba(236,72,153,.12),inset 0 0 16px rgba(99,58,180,.22);backdrop-filter:blur(12px);font-family:monospace;z-index:20;';
          var style=document.getElementById('amarDigitalClockStyle');
          if(!style){style=document.createElement('style');style.id='amarDigitalClockStyle';document.head.appendChild(style)}
          style.textContent=`
            #clock.amar-digital-clock:before{content:"";position:absolute;inset:-55%;background:conic-gradient(from 20deg,transparent 0deg,#f1c44c99 48deg,transparent 82deg,#7c3aed99 130deg,transparent 178deg,#ec489999 228deg,transparent 278deg,#312e8199 325deg,transparent 360deg);animation:adcSpin 8s linear infinite;z-index:0}
            #clock.amar-digital-clock:after{content:"";position:absolute;inset:1px;border-radius:13px;background:linear-gradient(115deg,transparent 0%,rgba(241,196,76,.14) 25%,transparent 43%,rgba(236,72,153,.10) 72%,transparent 100%);z-index:1;pointer-events:none}
            #clock.amar-digital-clock .adc-time,#clock.amar-digital-clock .adc-date{position:relative;z-index:2;text-align:center;font-variant-numeric:tabular-nums;white-space:nowrap}
            #clock.amar-digital-clock .adc-time{font-size:16px;font-weight:1000;letter-spacing:1px;color:#f7e7b2;text-shadow:0 0 4px #f1c44c,0 0 9px #a855f7,0 0 15px #ec4899;line-height:18px}
            #clock.amar-digital-clock .adc-date{font-size:7px;font-weight:900;letter-spacing:1px;color:#d9d2ff;text-shadow:0 0 5px #7c3aed,0 0 8px #ec4899;line-height:9px}
            @keyframes adcSpin{to{transform:rotate(360deg)}}
            @keyframes adcPulse{50%{filter:brightness(1.12)}}
          `;
          function tick(){
            var d=new Date();
            var p=function(n){return String(n).padStart(2,'0')};
            var time=p(d.getHours())+':'+p(d.getMinutes())+':'+p(d.getSeconds());
            var date=p(d.getDate())+'/'+p(d.getMonth()+1)+'/'+d.getFullYear();
            var timeNode=c.querySelector('.adc-time');
            var dateNode=c.querySelector('.adc-date');
            if(timeNode)timeNode.textContent=time;
            if(dateNode)dateNode.textContent=date;
            c.style.animation='adcPulse 2.8s ease-in-out infinite';
            var delay=1000-(Date.now()%1000)+8;
            window.__amarDigitalClockTimer=setTimeout(tick,delay);
          }
          tick();
        })();
    """.trimIndent()
}
