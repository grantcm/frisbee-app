(ns frisbee-spa.app
  (:require [frisbee-spa.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
