(ns frisbee-spa.db.core
    (:require [monger.core :as mg]
              [monger.collection :as mc]
              [monger.operators :refer :all]
              [mount.core :refer [defstate]]
              [frisbee-spa.config :refer [env]]))

(defstate db*
  :start (-> env :database-url mg/connect-via-uri)
  :stop (-> db* :conn mg/disconnect))

(defstate db
  :start (:db db*))

(defn insert-point [point]
  (mc/insert db "points" point))

(defn update-point [id x-coordinate y-coordinate]
  (mc/update db "points" {:_id id}
             {$set {:x x-coordinate
                    :y y-coordinate}}))

(defn get-points []
  (mc/find db "points"))

(defn get-point [id]
  (mc/find-one-as-map db "points" {:_id id}))
