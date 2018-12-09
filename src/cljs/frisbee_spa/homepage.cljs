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
  (js/console.log "Drawing first endzone")
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
  (js/console.log "Drawing second endzone")
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
  (js/console.log "Drawing main field")
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
           :field-width field-width
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

;;Since the click event takes the coordinates of the containing div, normalize to the canvas coordinates
(defn normalize-click-to-canvas [click-x click-y]
  (let [canvas-offset-x (.-offsetLeft (:canvas @canvas-state))
        canvas-offset-y (.-offsetTop (:canvas @canvas-state))]
    {:x (- click-x canvas-offset-x)
     :y (- click-y canvas-offset-y)}))

(defn coordinate-in-field [ coordinates ]
  (let [field-start-x (:field-start-x @field-state)
        field-start-y (:field-start-y @field-state)
        field-end-x (+ field-start-x (:field-width @field-state))
        field-end-y (+ field-start-y (:field-height @field-state))]
    (if (=
          (>= (:x coordinates) field-start-x)
          (>= (:y coordinates) field-start-y)
          (<= (:x coordinates) field-end-x)
          (<= (:y coordinates) field-end-y))
      true
      false)))

(defn coordinate-in-endzone-1 [ coordinates ]
  (let [endzone-start-x (:first-endzone-start-x @field-state)
        endzone-start-y (:first-endzone-start-y @field-state)
        endzone-end-x (+ endzone-start-x (:field-width @field-state))
        endzone-end-y (+ endzone-start-y (:endzone-height @field-state))]
    (if (=
          (>= (:x coordinates) endzone-start-x)
          (>= (:y coordinates) endzone-start-y)
          (<= (:x coordinates) endzone-end-x)
          (<= (:y coordinates) endzone-end-y))
      true
      false)))

(defn coordinate-in-endzone-2 [ coordinates ]
  (let [endzone-start-x (:second-endzone-start-x @field-state)
        endzone-start-y (:second-endzone-start-y @field-state)
        endzone-end-x (+ endzone-start-x (:field-width @field-state))
        endzone-end-y (+ endzone-start-y (:endzone-height @field-state))]
    (if (=
          (>= (:x coordinates) endzone-start-x)
          (>= (:y coordinates) endzone-start-y)
          (<= (:x coordinates) endzone-end-x)
          (<= (:y coordinates) endzone-end-y))
      true
      false)))

(defn translate-click-to-field [ coordinates ]
  (if (neg? (:x coordinates))
    "Out of bounds"
    (if (neg? (:y coordinates))
      "Out of bounds"
      (if (coordinate-in-field coordinates)
        "In main field"
        (if (coordinate-in-endzone-1 coordinates)
          "In endzone 1"
          (if (coordinate-in-endzone-2 coordinates)
            "In endzone 2"
            "Out of bounds"))))))

;;Click evt coordinates are relative to the containing div, so first calculate the
;;0,0 position of the canvas
;;Click includes the margin and border of the div
(defn handle-canvas-click [ evt ]
  (let [click-x (.-clientX evt)
        click-y (.-clientY evt)
        coordinates (normalize-click-to-canvas click-x click-y)]
    (js/console.log "Click at X: " click-x " Y: " click-y)
    (js/console.log "Normalized click" coordinates)
    (js/console.log (translate-click-to-field coordinates))))

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
       [:div {:class "field-container" :on-click handle-canvas-click}
        [:canvas (assoc (dissoc @canvas-state :canvas) :class "field-canvas")]])}))

(defn home-page []
  (js/console.log "Rendering home page")
  [:div {:class "flex-container"}
   [field-canvas]
   [right-panel]])
