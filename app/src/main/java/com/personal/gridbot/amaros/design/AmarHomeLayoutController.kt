package com.personal.gridbot.amaros.design

/** Independent controller for ten radically different animated Amar home layouts. */
object AmarHomeLayoutController {
    const val DEFAULT_LAYOUT = AmarSharedUiContract.LAYOUT_MIN
    const val LAYOUT_COUNT = AmarSharedUiContract.LAYOUT_MAX

    fun applyJavascript(layout: Int): String {
        val v = layout.coerceIn(AmarSharedUiContract.LAYOUT_MIN, AmarSharedUiContract.LAYOUT_MAX)
        return """
            (function(){
              var b=document.body;
              for(var i=1;i<=${AmarSharedUiContract.LAYOUT_MAX};i++)b.classList.remove('amar-v'+i);
              b.classList.add('amar-v$v');
              var s=document.getElementById('amarLayoutStyle');
              if(!s){s=document.createElement('style');s.id='amarLayoutStyle';document.head.appendChild(s)}
              s.textContent=`
                .scene{transition:background 1s ease,filter .8s ease}
                .core,.node,.action,.pill{transition:transform .55s ease,box-shadow .55s ease,border-color .55s ease,background .55s ease}
                .amar-v1 .coreWrap{top:44%}.amar-v1 .nodes{inset:15% 3% 20%}
                .amar-v2 .scene{background:radial-gradient(circle at 50% 50%,#eafcff,#bdeaf5 38%,#d7ccff 68%,#f8fbff)}.amar-v2 .coreWrap{top:47%;width:min(58vw,260px)}.amar-v2 .core{background:linear-gradient(145deg,rgba(255,255,255,.82),rgba(220,242,255,.42));backdrop-filter:blur(18px);border-color:#8c7cff;box-shadow:0 0 40px #8c7cff55}.amar-v2 .node{background:rgba(255,255,255,.48);backdrop-filter:blur(16px);color:#26345c;border-color:#6edcff}.amar-v2 .panel{bottom:5%;grid-template-columns:repeat(2,1fr)}
                .amar-v3 .scene{background:conic-gradient(from 90deg at 50% 45%,#071d3b,#132b62,#371b63,#071d3b)}.amar-v3 .coreWrap{top:39%;width:min(52vw,240px)}.amar-v3 .nodes{inset:7% 8% 30%}.amar-v3 .n1{left:0;top:18%}.amar-v3 .n2{right:0;top:18%}.amar-v3 .n3{left:8%;bottom:5%}.amar-v3 .n4{right:8%;bottom:5%}.amar-v3 .n5{top:0}.amar-v3 .n6{bottom:-3%}.amar-v3 .panel{left:8%;right:8%;bottom:1%}
                .amar-v4 .scene{background:linear-gradient(135deg,#fff6dc,#ffe7f2 42%,#dff7ff)}.amar-v4 .coreWrap{top:48%;width:min(50vw,230px)}.amar-v4 .core{background:rgba(255,255,255,.62);backdrop-filter:blur(22px);border-color:#ff8ec7;box-shadow:0 18px 60px #ff8ec755}.amar-v4 .node{background:rgba(255,255,255,.65);color:#5b3353;border-color:#ff9dcc}.amar-v4 .panel{bottom:4%}
                .amar-v5 .scene{background:radial-gradient(circle at 50% 40%,#123b3b,#062322 45%,#041514)}.amar-v5 .coreWrap{top:41%;width:min(70vw,300px)}.amar-v5 .core{border-radius:28%;transform:rotate(45deg)}.amar-v5 .core>*{transform:rotate(-45deg)}.amar-v5 .nodes{inset:10% 2% 24%}.amar-v5 .node{border-radius:24%;transform:rotate(45deg)}.amar-v5 .node:active{transform:rotate(45deg) scale(.9)}
                .amar-v6 .scene{background:linear-gradient(180deg,#071225,#12345b 48%,#05111e)}.amar-v6 .coreWrap{top:50%;width:min(44vw,210px)}.amar-v6 .orbit{border-style:dashed;animation-duration:9s}.amar-v6 .nodes{inset:3% 12% 28%}.amar-v6 .node{width:70px;height:70px;border-radius:12px;transform:skewY(-8deg)}.amar-v6 .n1{left:2%;top:28%}.amar-v6 .n2{right:2%;top:28%}.amar-v6 .n3{left:2%;bottom:12%}.amar-v6 .n4{right:2%;bottom:12%}.amar-v6 .n5{top:4%}.amar-v6 .n6{bottom:2%}.amar-v6 .panel{bottom:0;left:12%;right:12%;grid-template-columns:repeat(2,1fr)}
                .amar-v7 .scene{background:radial-gradient(circle at 50% 50%,#3b0716,#1d0625 45%,#10051b)}.amar-v7 .coreWrap{top:45%;width:min(62vw,280px)}.amar-v7 .core{border-color:#ff5b8a;box-shadow:0 0 70px #ff2e7555}.amar-v7 .node{background:linear-gradient(145deg,#3b102b,#14081e);border-color:#ff5b8a}.amar-v7 .nodes{inset:18% 0 22%}.amar-v7 .panel{bottom:2%;background:rgba(25,4,25,.45);padding:7px;border-radius:24px}
                .amar-v8 .scene{background:linear-gradient(120deg,#10102c,#24104b 48%,#063b54)}.amar-v8 .coreWrap{left:67%;top:43%;width:min(48vw,220px)}.amar-v8 .nodes{inset:13% 38% 18% 2%}.amar-v8 .n1{left:0;top:5%}.amar-v8 .n2{left:48%;top:15%}.amar-v8 .n3{left:0;top:42%}.amar-v8 .n4{left:48%;top:53%}.amar-v8 .n5{left:22%;top:27%}.amar-v8 .n6{left:22%;bottom:2%}.amar-v8 .panel{right:4%;left:56%;bottom:5%;grid-template-columns:repeat(2,1fr)}
                .amar-v9 .scene{background:radial-gradient(circle at 50% 45%,#fff,#dff9ff 28%,#c9fff0 55%,#e8ddff)}.amar-v9 .coreWrap{top:43%;width:min(46vw,215px)}.amar-v9 .core{background:rgba(255,255,255,.5);backdrop-filter:blur(30px);border:2px solid rgba(255,255,255,.9);box-shadow:0 20px 80px #6edcff66}.amar-v9 .node{background:rgba(255,255,255,.42);backdrop-filter:blur(20px);color:#194050;border-color:#9af4ff}.amar-v9 .nodes{inset:5% 5% 27%}.amar-v9 .panel{background:rgba(255,255,255,.3);backdrop-filter:blur(20px);padding:8px;border-radius:25px;bottom:2%}
                .amar-v10 .scene{background:linear-gradient(125deg,#061a29,#183c62 35%,#5a247d 70%,#091a35)}.amar-v10 .top{top:8%;left:12%;right:12%}.amar-v10 .coreWrap{top:54%;left:35%;width:min(48vw,225px)}.amar-v10 .core{border-radius:18%;transform:perspective(700px) rotateX(12deg) rotateY(-18deg);box-shadow:-25px 30px 70px #00d9ff44,25px -20px 70px #b46cff55}.amar-v10 .nodes{inset:18% 3% 26%}.amar-v10 .node{border-radius:18px;transform:perspective(500px) rotateY(-12deg)}.amar-v10 .panel{left:42%;right:3%;bottom:3%;grid-template-columns:repeat(2,1fr)}
                .amar-v1 .coreWrap,.amar-v2 .coreWrap,.amar-v3 .coreWrap,.amar-v4 .coreWrap,.amar-v5 .coreWrap,.amar-v6 .coreWrap,.amar-v7 .coreWrap,.amar-v8 .coreWrap,.amar-v9 .coreWrap,.amar-v10 .coreWrap{animation:amarHomeFloat 4s ease-in-out infinite}
                @keyframes amarHomeFloat{50%{translate:0 -7px;filter:brightness(1.08)}}
              `;
            })();
        """.trimIndent() + "\n" + AmarAiOrbController.script()
    }
}