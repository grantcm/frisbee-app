(ns user
  (:require [frisbee-spa.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [frisbee-spa.figwheel :refer [start-fw stop-fw cljs]]
            [frisbee-spa.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'frisbee-spa.core/repl-server))

(defn stop []
  (mount/stop-except #'frisbee-spa.core/repl-server))

(defn restart []
  (stop)
  (start))


