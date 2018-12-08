(ns frisbee-spa.homepage
  (:require [reagent.core :as r]))

;; Declare constants for field size in meters
(def ^:const fieldLength (int 64))
(def ^:const endzoneLength (int 23))
(def ^:const fullFieldLength (+ fieldLength endzoneLength endzoneLength))
(def ^:const fieldWidth (int 37))
(def ^:const brickMark (int 18))
(def ^:const lengthToWidth (/ fullFieldLength fieldWidth))

;;Used for updating render when window is resized
(def window-width (r/atom nil))
;;State for canvas field
(def canvas-state (r/atom {:minheight fullFieldLength :minwidth fieldWidth}))
(def field-state (r/atom nil))
;;Dom node for canvas container
(def dom-node (r/atom nil))

;;Template code for stat panel
(defn lister [items]
  [:ul
   (for [item items]
     ^{:key item} [:li "Item " item])])

(defn right-panel []
  [:div {:class "stat-container"}
   [lister (range 3)]])

;;Draw on canvas code
(defn draw-first-endzone []
  (let [ctx (:ctx @field-state)]
    (set! (.-fillStyle ctx) "#196f0c")
    (.fillRect ctx
               (:first-endzone-start-x @field-state)
               (:first-endzone-start-y @field-state)
               (:field-width @field-state)
               (:endzone-height @field-state))
    (set! (.-strokeStyle ctx) "#000000")
    (set! (.-lineWidth ctx) 3)
    (.strokeRect ctx
                 (:first-endzone-start-x @field-state)
                 (:first-endzone-start-y @field-state)
                 (:field-width @field-state)
                 (:endzone-height @field-state))))

(defn draw-second-endzone []
  (let [ctx (:ctx @field-state)]
    (set! (.-fillStyle ctx) "#196f0c")
    (.fillRect ctx
               (:second-endzone-start-x @field-state)
               (:second-endzone-start-y @field-state)
               (:field-width @field-state)
               (:endzone-height @field-state))
    (set! (.-strokeStyle ctx) "#000000")
    (set! (.-lineWidth ctx) 3)
    (.strokeRect ctx
                 (:second-endzone-start-x @field-state)
                 (:second-endzone-start-y @field-state)
                 (:field-width @field-state)
                 (:endzone-height @field-state))))

(defn draw-main-field []
  (let [ctx (:ctx @field-state)]
    (set! (.-fillStyle ctx) "#196f0c")
    (.fillRect ctx
               (:field-start-x @field-state)
               (:field-start-y @field-state)
               (:field-width @field-state)
               (:field-height @field-state))
    (set! (.-strokeStyle ctx) "#000000")
    (set! (.-lineWidth ctx) 3)
    (.strokeRect ctx
                 (:field-start-x @field-state)
                 (:field-start-y @field-state)
                 (:field-width @field-state)
                 (:field-height @field-state))))

(defn draw-canvas-objects []
  (js/console.log "Drawing field with ratio: " (:drawing-ratio @field-state))
  (draw-first-endzone)
  (draw-main-field)
  (draw-second-endzone))

;;Sets the variables that define the field state on the canvas
(defn set-vertical-field-state []
  (js/console.log "Setting field state")
  (let [ctx (.getContext (:canvas @canvas-state) "2d")
        field-width (:width @canvas-state)
        drawing-ratio (/ field-width fieldWidth)]
    (js/console.log ctx field-width drawing-ratio)
    (swap! field-state assoc
           :ctx ctx
           :drawing-ratio drawing-ratio
           :first-endzone-start-x 0
           :first-endzone-start-y 0
           :endzone-height (* endzoneLength drawing-ratio)
           :field-start-x 0
           :field-start-y (* endzoneLength drawing-ratio)
           :field-height (* fieldLength drawing-ratio)
           :field-width (* field-width drawing-ratio)
           :second-endzone-start-x 0
           :second-endzone-start-y (+ (* endzoneLength drawing-ratio) (* fieldLength drawing-ratio)))
    (js/console.log @field-state)))

;;Uses atom @dom-node to set the canvas to the width from the parent
;;If the height of the inner window is greater than the proportional height
;;from the width, use the proportional height, otherwise use the height to calculate width
(defn set-canvas-state []
  (let [window-width (.-clientWidth @dom-node)
        window-height (- (.-innerHeight js/window) 100)]
    (js/console.log window-height window-width)
    (swap! canvas-state assoc
           :canvas (.-firstChild @dom-node)
           :height (if (> window-height (* window-width lengthToWidth))
                     (* window-width lengthToWidth)
                     window-height)
           :width (if (> window-height (* window-width lengthToWidth))
                    window-width
                    (/ window-height lengthToWidth)))
    (js/console.log @canvas-state)
    (set-vertical-field-state)))

;;Resize listener for window
;;Calls set canvas state to update canvas when resized
(defn on-window-resize [evt]
  (reset! window-width (.-innerWidth js/window))
  (set-canvas-state))

;;Renders the canvas element
(defn field-canvas []
  (r/create-class
    {:component-did-update
     (fn [this]
       ;;Draw canvas objects when dom node mounts and field state is created
       (if @field-state (draw-canvas-objects) (js/console.log "field-state not initialized")))
     :component-did-mount
     (fn [this]
       ;;When component mounts set dom node containing canvas
       (reset! dom-node (r/dom-node this))
       (set-canvas-state)
       (js/console.log "Adding resize listener to app")
       (.addEventListener js/window "resize" on-window-resize))
     :reagent-render
     (fn []
       ;;Render whenever window-width changes
       @window-width
       [:div {:class "field-container"}
        [:canvas @canvas-state]])}))

(defn home-page []
  (js/console.log "Rendering home page")
  [:div {:class "flex-container"}
   [field-canvas]
   [right-panel]])
