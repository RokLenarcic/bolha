(ns bolha
  (:require [clojure.tools.cli :as cli]
            [clj-http.client :as client]
            [clojure.string :refer [join]]
            [net.cgrand.enlive-html :as html]
            [postal.core :as postal]
            [clojure.java.io :as io])
  (:import (java.io StringReader))
  (:gen-class))

(defn render-ads [ads]
  (->> ads
       (map #(vector
               :table {:style "border: thick double black; margin: 15px"}
               [:tr {:style "border-style: border-style: double double none double; border-width: thick"}
                [:td [:img {:src (:image %)}]]
                [:td [:a {:href (:href %)} (:title %)]]
                [:td (:price %)]]
               [:tr {:style "margin-bottom: 30px; border-style: none double double double; border-width: thick"}
                [:td {:colspan "3"} (:desc %)]]))
       (cons :div)
       vec
       html/html
       html/emit*
       (apply str)))

(defn ads [htm]
  (map
    #(hash-map
       :title (-> (html/select % [:h3.entity-title :a]) html/texts join)
       :desc (->> (html/select % [:div.entity-description-main]) html/texts join (.trim))
       :href (->> (html/select % [:h3.entity-title :a]) first :attrs :href (str "https://www.bolha.com"))
       :image (->> (html/select % [:img.entity-thumbnail-img]) first :attrs :data-src (str "https:" ))
       :price (-> (html/select % [:li.price-item]) html/texts join (.trim)))
    (html/select
      htm
      [:div.EntityList--Regular :article.entity-body.cf])))

(defn scrape-ads [params]
  (let [scraped (some->
                  (client/get "https://www.bolha.com/"
                              {:headers {"User-Agent" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.2 Safari/605.1.15"}
                               :query-params {"ctl" "search_ads"
                                              "keywords" (:query params)
                                              "sort" "new"}})
                        :body
                        StringReader.
                        html/html-resource)
        log (when (.exists (io/file (:lock-file params))) (read-string (slurp (:lock-file params))))
        actual-ads (take-while #(not= log %) (ads scraped))]
    (println actual-ads)
    (when (not-empty actual-ads)
      (postal/send-message
        (select-keys params [:host :user :pass :port :tls :ssl])
        {:from    (:mail params)
         :to      (:mail params)
         :subject (str "Bolha zadnji oglasi za " (:query params))
         :body    [{:type    "text/html; charset=utf-8"
                    :content (render-ads actual-ads)}]})
      (spit (:lock-file params) (prn-str (first actual-ads))))))

(def cli-options
  [["-h" "--host HOST" "SMTP host - required"]
   ["-m" "--mail MAIL_ADDRESS" "Mail address to use as sender and receiver. - required"]
   ["-u" "--user USER" "Mail username - required"]
   ["-P" "--pass PASSWORD" "Mail password or App token - required"]
   ["-p" "--port PORT" "SMTP port" :parse-fn #(Integer/parseInt %)]
   ["-T" "--tls" "SMTP TLS enabled?"]
   ["-S" "--ssl" "SMTP SSL enabled?"]
   ["-q" "--query QUERY" "Search query e.g. 'lego' - required or query-url"]
   ["-l" "--lock-file FILE" "File used to track last sent ad" :default "bolha.lock"]])

(defn -main [& args]
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)
        missing-opts (vec (filter (complement (into #{} (keys options))) [:host :mail :user :pass :query]))]
    (cond (not-empty missing-opts)
          (println (str "Missing required options " missing-opts "\n\n" summary))
          (not-empty errors)
          (do (println (str (join \newline errors) "\n\n" summary))
              (System/exit 1))
          :default (scrape-ads options))))
