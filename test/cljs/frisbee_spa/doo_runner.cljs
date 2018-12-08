(ns frisbee-spa.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [frisbee-spa.core-test]))

(doo-tests 'frisbee-spa.core-test)

