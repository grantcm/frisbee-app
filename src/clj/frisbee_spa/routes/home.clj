(ns frisbee-spa.routes.home
  (:require [frisbee-spa.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [response status]]
            [clojure.java.io :as io]
            [frisbee-spa.db.core :as db]))

(defn home-page []
  (layout/render "home.html"))

(defn add-click-data-point! [ point ]
  (println point)
  (db/insert-point point)
  (response {:status :success}))

(defroutes home-routes
  (GET "/" []
       (home-page))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8")))
  (POST "/send-click" request (add-click-data-point! (:params request))))

