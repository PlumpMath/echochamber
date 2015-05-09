(ns ecos.views.common
  ;;(:require [cemerick.austin.repls :refer [browser-connected-repl-js]])
  (:use [hiccup.core :refer :all]))

(def airbrake-script
  [:script
   {:type "text/javascript"
    :src "/js/vendor/airbrake-shim.js"
    :data-airbrake-project-id (or (System/getenv "AIRBRAKE_API_KEY")
                                  #_(carica/config :airbrake :id))
    :data-airbrake-project-key (or (System/getenv "AIRBRAKE_API_KEY")
                                   #_(carica/config :airbrake :key))
    :data-airbrake-environment-name (if (System/getenv "PRODUCTION")
                                      "production"
                                      "development")}])

(def olark-script
  (list "<!-- begin olark code -->"
        [:script
         {:type "text/javascript", :data-cfasync "false"}
         (str "/*{literal}<![CDATA[*/\nwindow.olark||(function(c){var f=window,d=document,l=f.location.protocol==\"https:\"?\"https:\":\"http:\",z=c.name,r=\"load\";var nt=function(){f[z]=function(){(a.s=a.s||[]).push(arguments)};var a=f[z]._={},q=c.methods.length;while(q--){(function(n){f[z][n]=function(){f[z](\"call\",n,arguments)}})(c.methods[q])}a.l=c.loader;a.i=nt;a.p={0:+new Date};a.P=function(u){a.p[u]=new Date-a.p[0]};function s(){a.P(r);f[z](r)}f.addEventListener?f.addEventListener(r,s,false):f.attachEvent(\"on\"+r,s);var ld=function(){function p(hd){hd=\"head\";return[\"<\",hd,\"></\",hd,\"><\",i,' onl' + 'oad=\"var d=',g,\";d.getElementsByTagName('head')[0].\",j,\"(d.\",h,\"('script')).\",k,\"='\",l,\"//\",a.l,\"'\",'\"',\"></\",i,\">\"].join(\"\")}var i=\"body\",m=d[i];if(!m){return setTimeout(ld,100)}a.P(1);var j=\"appendChild\",h=\"createElement\",k=\"src\",n=d[h](\"div\"),v=n[j](d[h](z)),b=d[h](\"iframe\"),g=\"document\",e=\"domain\",o;n.style.display=\"none\";m.insertBefore(n,m.firstChild).id=z;b.frameBorder=\"0\";b.id=z+\"-loader\";if(/MSIE[ ]+6/.test(navigator.userAgent)){b.src=\"javascript:false\"}b.allowTransparency=\"true\";v[j](b);try{b.contentWindow[g].open()}catch(w){c[e]=d[e];o=\"javascript:var d=\"+g+\".open();d.domain='\"+d.domain+\"';\";b[k]=o+\"void(0);\"}try{var t=b.contentWindow[g];t.write(p());t.close()}catch(x){b[k]=o+'d.write(\"'+p().replace(/\"/g,String.fromCharCode(92)+'\"')+'\");d.close();'}a.P(2)};ld()};nt()})({loader: \"static.olark.com/jsclient/loader0.js\",name:\"olark\",methods:[\"configure\",\"extend\",\"declare\",\"identify\"]});\n/* custom configuration goes here (http://www.olark.com/documentation) */\nolark.identify('"
              #_ (carica/config :support :olark :public-key)
              "');/*]]>{/literal}*/\n\n\n")]
        [:noscript
         [:a
          {:target "_blank",
           :title "Contact us",
           :href (str "https://www.olark.com/site/"
                      #_(carica/config :support :olark :public-key) "/contact")}
          "Questions? Feedback?"]
         " powered by "
         [:a
          {:title "Olark live chat software",
           :href "http://www.olark.com?welcome"}
          "Olark live chat software"]]
        "<!-- end olark code -->"))

(def mixpanel-script
  [:script {:type "text/javascript"}
   (str "(function(e,b){if(!b.__SV){var a,f,i,g;window.mixpanel=b;a=e.createElement(\"script\");a.type=\"text/javascript\";a.async=!0;a.src=(\"https:\"===e.location.protocol?\"https:\":\"http:\")+'//cdn.mxpnl.com/libs/mixpanel-2.2.min.js';f=e.getElementsByTagName(\"script\")[0];f.parentNode.insertBefore(a,f);b._i=[];b.init=function(a,e,d){function f(b,h){var a=h.split(\".\");2==a.length&&(b=b[a[0]],h=a[1]);b[h]=function(){b.push([h].concat(Array.prototype.slice.call(arguments,0)))}}var c=b;\"undefined\"!==typeof d?c=b[d]=[]:d=\"mixpanel\";c.people=c.people||[];c.toString=function(b){var a=\"mixpanel\";\"mixpanel\"!==d&&(a+=\".\"+d);b||(a+=\" (stub)\");return a};c.people.toString=function(){return c.toString(1)+\".people (stub)\"};i=\"disable track track_pageview track_links track_forms register register_once alias unregister identify name_tag set_config people.set people.set_once people.increment people.append people.track_charge people.clear_charges people.delete_user\".split(\" \");for(g=0;g<i.length;g++)f(c,i[g]);b._i.push([a,e,d])};b.__SV=1.2}})(document,window.mixpanel||[]);mixpanel.init('" #_(carica/config :metrics :mixpanel :public-key)  "');" )])

(def stripe-js-script
  [:script {:type "text/javascript" :src "https://js.stripe.com/v2/"}])

(defn layout [& content]
  [:html
   [:head
    [:title "Ecos - Next-gen CMS"]
    [:link {:rel "stylesheet", :href "/css/vendor/bootstrap/css/bootstrap.min.css"}]
    [:link {:rel "stylesheet", :href "/css/news.css"}]
    #_[:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0;minimal-ui;"}]
    #_(when (= :prod #_(carica/config :env-name))
      mixpanel-script)]
   [:body
    ;;[:link.css-styles {:rel "stylesheet", :href "/css/bootstrap.min.css"}]
    ;;[:link.css-styles {:rel "stylesheet", :href "/css/styles.css"}]
    [:div.alerts-container]
    content
    (when true ;;(not= :prod #_(carica/config :env-name))
      ;;[:script (browser-connected-repl-js)]
      )]])
