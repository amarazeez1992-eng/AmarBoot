package com.personal.gridbot.amaros.design

/** Independent premium digital clock controller for the home WebView. */
object AmarDigitalClockController {
    fun script(): String = """
        (function(){
          var c=document.getElementById('clock');
          if(!c)return;
          if(window.__amarDigitalClockTimer)clearInterval(window.__amarDigitalClockTimer);
          c.className='amar-digital-clock';
          c.innerHTML='<span class="adc-time">00:00:00</span><span class="adc-date">--/--/----</span>';
          c.style.cssText='position:relative;width:112px;height:62px;border-radius:18px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:2px;box-sizing:border-box;overflow:hidden;direction:ltr;background:linear-gradient(145deg,rgba(8,20,35,.96),rgba(31,20,68,.94));border:1px solid rgba(102,231,255,.78);box-shadow:0 0 0 1px rgba(177,108,255,.25),0 0 28px rgba(0,217,255,.34),0 0 44px rgba(148,91,255,.18),inset 0 0 22px rgba(104,211,255,.13);backdrop-filter:blur(14px);font-family:monospace;z-index:20;';
          var style=document.getElementById('amarDigitalClockStyle');
          if(!style){style=document.createElement('style');style.id='amarDigitalClockStyle';document.head.appendChild(style)}
          style.textContent=`
            #clock.amar-digital-clock:before{content:"";position:absolute;inset:-35%;background:conic-gradient(from 0deg,transparent,#00eaff55,transparent,#b86cff55,transparent,#00eaff44,transparent);animation:adcSpin 7s linear infinite;z-index:0}
            #clock.amar-digital-clock:after{content:"";position:absolute;left:-35%;top:0;width:35%;height:100%;background:linear-gradient(90deg,transparent,#ffffffaa,transparent);transform:skewX(-18deg);animation:adcShine 3.2s ease-in-out infinite;z-index:3}
            #clock.amar-digital-clock .adc-time,#clock.amar-digital-clock .adc-date{position:relative;z-index:2;text-align:center;font-variant-numeric:tabular-nums;white-space:nowrap}
            #clock.amar-digital-clock .adc-time{font-size:18px;font-weight:1000;letter-spacing:1.5px;color:#8ff7ff;text-shadow:0 0 6px #00eaff,0 0 14px #8b6cff,0 0 22px #00eaff;line-height:20px}
            #clock.amar-digital-clock .adc-date{font-size:8px;font-weight:900;letter-spacing:1.2px;color:#d6c9ff;text-shadow:0 0 7px #a86cff;line-height:11px}
            @keyframes adcSpin{to{transform:rotate(360deg)}}
            @keyframes adcShine{0%,35%{left:-35%;opacity:0}50%{opacity:1}70%,100%{left:115%;opacity:0}}
            @keyframes adcPulse{50%{filter:brightness(1.18)}}
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
          }
          tick();
          window.__amarDigitalClockTimer=setInterval(tick,1000);
        })();
    """.trimIndent()
}
