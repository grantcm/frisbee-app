(ns frisbee-spa.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[frisbee-spa started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[frisbee-spa has shut down successfully]=-"))
   :middleware identity})
