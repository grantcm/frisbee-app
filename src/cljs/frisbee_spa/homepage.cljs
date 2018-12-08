(ns frisbee-spa.homepage
  (:require [reagent.core :as r]))

;; Declare constants for field size in meters
(def ^:const fieldLength (int 64))
(def ^:const endzoneLength (int 23))
(def ^:const fullFieldLength (+ fieldLength endzoneLength endzoneLength))
(def ^:const fieldWidth (int 37))
(def ^:const brickMark (int 18))
(def ^:const lengthToWidth (/ fullFieldLength fieldWidth))

;;Template code for stat panel

(defn lister [items]
  [:ul
   (for [item items]
     ^{:key item} [:li "Item " item])])

(defn right-panel []
  [:div {:class "stat-container"}
   [lister (range 3)]])

;;TODO refactor to new class just for field canvas
;;TODO could use atom for storing state

(defn draw-first-endzone [ctx canvas-width ratio]
  (set! (.-fillStyle ctx) "#196f0c")
  (.fillRect ctx 0 0 (* canvas-width ratio) (* endzoneLength ratio))
  (set! (.-strokeStyle ctx) "#000000")
  (set! (.-lineWidth ctx) 3)
  (.strokeRect ctx 0 0 (* canvas-width ratio) (* endzoneLength ratio)))

(defn draw-second-endzone [ctx canvas-width ratio]
  (let [start-x (+ (* endzoneLength ratio) (* fieldLength ratio))]
  (set! (.-fillStyle ctx) "#196f0c")
  (.fillRect ctx 0 start-x (* canvas-width ratio)  (+ start-x (* endzoneLength ratio)))
  (set! (.-strokeStyle ctx) "#000000")
  (set! (.-lineWidth ctx) 3)
  (.strokeRect ctx 0 start-x (* canvas-width ratio)  (+ start-x (* endzoneLength ratio)))))

(defn draw-main-field [ctx canvas-width ratio]
  (set! (.-fillStyle ctx) "#196f0c")
  (.fillRect ctx 0 (* endzoneLength ratio) (* canvas-width ratio)  (* fieldLength ratio))
  (set! (.-strokeStyle ctx) "#000000")
  (set! (.-lineWidth ctx) 3)
  (.strokeRect ctx 0 (* endzoneLength ratio) (* canvas-width ratio)  (* fieldLength ratio)))

(defn draw-canvas-objects [canvas]
  (let [ ctx (.getContext canvas "2d")
        w (.-clientWidth canvas)
        h (.-clientHeight canvas)
        ratio (/ w fieldWidth)]
  (js/console.log "Drawing field with ratio: " ratio)
  (draw-first-endzone ctx w ratio)
  (draw-main-field ctx w ratio)
  (draw-second-endzone ctx w ratio)))

(def window-width (r/atom nil))

(defn on-window-resize [ evt ]
  (reset! window-width (.-innerWidth js/window)))

;;Uses atom @dom-node to set the canvas to the width from the parent
;;If the height of the inner window is greater than the proportional height
;;from the width, use the proportional height, otherwise use the height to calculate width
(defn get-canvas-size [node]
  (let [width (.-clientWidth node)
        windowHeight (- (.-innerHeight js/window) 100)]
    (js/console.log windowHeight width)
    (if (> windowHeight (* width lengthToWidth))
      (do (js/console.log (* width lengthToWidth) width)
          {:height (* width lengthToWidth)
           :width width
           :minheight fullFieldLength
           :minwidth fieldWidth})
      (do (js/console.log windowHeight (/ windowHeight lengthToWidth))
          {:height windowHeight
           :width (/ windowHeight lengthToWidth)
           :minheight fullFieldLength
           :minwidth fieldWidth}))))

;;Renders the canvas element
(defn field-canvas []
  (let [dom-node (r/atom nil)]
    (r/create-class
      {:component-did-update
       (fn [ this ]
         (draw-canvas-objects (.-firstChild @dom-node)))
       :component-did-mount
       (fn [ this ]
         (reset! dom-node (r/dom-node this))
         (js/console.log "Adding resize listener to app")
         (.addEventListener js/window "resize" on-window-resize))
       :reagent-render
       (fn [ ]
          @window-width
          [:div {:class "field-container"}
            [:canvas (if-let [node @dom-node]
                       (get-canvas-size node))]])})))

(defn home-page []
  (js/console.log "Rendering home page")
  [:div {:class "flex-container"}
   [field-canvas]
   [right-panel]])
