(ns clojure-maze.core
  (:gen-class))

(def size 10)

(defn create-rooms []
  (vec
    (for [row (range 0 size)]
      (vec
        (for [col (range 0 size)]
          {:row row
           :col col
           :visited? false
           :bottom? true
           :right? true
           :end? false})))))

(defn possible-neighbors [rooms row col]
  (vec
    (filter
      (fn [room]
        (and room (= false (:visited? room))))
      [(get-in rooms [(- row 1) col])
       (get-in rooms [(+ row 1) col])
       (get-in rooms [row (- col 1)])
       (get-in rooms [row (+ col 1)])])))

(defn random-neighbor [rooms row col]
  (let [neighbors (possible-neighbors rooms row col)]
    (if (pos? (count neighbors))
      (rand-nth neighbors)
      nil)))

(defn reagan [rooms old-row old-col new-row new-col]
  (cond
    (< new-row old-row)
    (assoc-in rooms [new-row new-col :bottom?] false)
    (> new-row old-row)
    (assoc-in rooms [old-row old-col :bottom?] false)
    (< new-col old-col)
    (assoc-in rooms [new-row new-col :right?] false)
    (> new-col old-col)
    (assoc-in rooms [old-row old-col :right?] false)))

(declare create-maze)

(def hit-dead-end? (atom false))

(defn create-maze-loop [rooms old-row old-col new-row new-col]
  (let [new-rooms (reagan rooms old-row old-col new-row new-col)
        new-rooms (create-maze new-rooms new-row new-col)]
      (if (= rooms new-rooms)
        (if (deref hit-dead-end?)
          rooms
          (do
            (reset! hit-dead-end? true)
            (assoc-in rooms [old-row old-col :end?] true)))
        (create-maze-loop new-rooms old-row old-col new-row new-col))))

(defn create-maze [rooms row col]
  (let [rooms (assoc-in rooms [row col :visited?] true)
        next-room (random-neighbor rooms row col)]
    (if next-room
      (create-maze-loop rooms row col (:row next-room) (:col next-room))
      rooms)))

(defn -main [& args]
  (reset! hit-dead-end? false)
  (let [rooms (create-rooms)
        rooms (create-maze rooms 0 0)]
    (doseq [row rooms]
      (print " _"))
    (println)
    (doseq [row rooms]
      (print "|")
      (doseq [room row]
        (cond
          (and (= 0 (:row room))
               (= 0 (:col room)))
          (print "o")
          (:end? room)
          (print "x")
          (:bottom? room)
          (print "_")
          :else
          (print " "))
        (if (:right? room)
          (print "|")
          (print " ")))
      (println))))
        

