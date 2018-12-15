(ns frisbee-spa.ajax
  (:require [ajax.core :as ajax]
            [ajax.core :refer [GET POST]]
            [luminus-transit.time :as time]
            [cognitect.transit :as transit]))

(defn local-uri? [{:keys [uri]}]
  (not (re-find #"^\w+?://" uri)))

(defn default-headers [request]
  (if (local-uri? request)
    (-> request
        (update :headers #(merge {"x-csrf-token" js/csrfToken} %)))
    request))

;; injects transit serialization config into request options
(defn as-transit [opts]
  (merge {:raw             false
          :format          :transit
          :response-format :transit
          :reader          (transit/reader :json time/time-deserialization-handlers)
          :writer          (transit/writer :json time/time-serialization-handlers)}
         opts))

(defn load-interceptors! []
  (swap! ajax/default-interceptors
         conj
         (ajax/to-interceptor {:name "default headers"
                               :request default-headers})))

(defn response-handle [ response ]
  (.log js/console (str response)))

(defn error-handler [ {:keys [status status-text]} ]
  (.log js/console (str "Error with request: " status "\nMessage: " status-text)))

(defn send-click [ params ]
  (js/console.log params)
  (POST "/send-click"
        {:format :json
         :params {:x (:x params)
                  :y (:y params)}
         :headers {"Accept" "application/json"
                   "x-csrf-token" js/csrfToken}
         :handler response-handle
         :error-handler error-handler}))


