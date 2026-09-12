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
          c.style.cssText='position:relative;width:96px;height:52px;border-radius:16px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:1px;box-sizing:border-box;overflow:hidden;direction:ltr;background:linear-gradient(145deg,rgba(8,20,35,.94),rgba(26,20,62,.88));border:1px solid rgba(102,231,255,.7);box-shadow:0 0 0 1px rgba(177,108,255,.22),0 0 22px rgba(0,217,255,.28),inset 0 0 18px rgba(104,211,255,.12);backdrop-filter:blur(12px);font-family:monospace;z-index:20;';
          var style=document.getElementById('amarDigitalClockStyle');
          if(!style){style=document.createElement('style');style.id='amarDigitalClockStyle';document.head.appendChild(style)}
          style.textContent=`
            #clock.amar-digital-clock:before{content:"";position:absolute;inset:-30%;background:conic-gradient(from 0deg,transparent,#00eaff55,transparent,#b86cff55,transparent);animation:adcSpin 7s linear infinite;z-index:0}
            #clock.amar-digital-clock:after{content:"";position:absolute;left:-30%;top:0;width:35%;height:100%;background:linear-gradient(90deg,transparent,#fff8,transparent);transform:skewX(-18deg);animation:adcShine 3.2s ease-in-out infinite;z-index:3}
            #clock.amar-digital-clock .adc-time,#clock.amar-digital-clock .adc-date{position:relative;z-index:2}
            #clock.amar-digital-clock .adc-time{font-size:17px;font-weight:1000;letter-spacing:1px;color:#8ff7ff;text-shadow:0 0 6px #00eaff,0 0 14px #8b6cff;line-height:18px}
            #clock.amar-digital-clock .adc-date{font-size:8px;font-weight:800;letter-spacing:1px;color:#d6c9ff;text-shadow:0 0 7px #a86cff;line-height:10px}
            @keyframes adcSpin{to{transform:rotate(360deg)}}
            @keyframes adcShine{0%,35%{left:-35%;opacity:0}50%{opacity:1}70%,100%{left:115%;opacity:0}}
            @keyframes adcPulse{50%{filter:brightness(1.18)}}
          `;
          function tick(){
            var d=new Date();
            var p=function(n){return String(n).padStart(2,'0')};
            c.querySelector('.adc-time').textContent=p(d.getHours())+':'+p(d.getMinutes())+':'+p(d.getSeconds());
            c.querySelector('.adc-date').textContent=p(d.getDate())+'/'+p(d.getMonth()+1)+'/'+d.getFullYear();
            c.style.animation='adcPulse 2.8s ease-in-out infinite';
          }
          tick(); window.__amarDigitalClockTimer=setInterval(tick,1000);
        })();
    """.trimIndent()
}
