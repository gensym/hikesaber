(ns hikesaber.util.integer-ids)

(defn make-idset []
  (atom [{} {}]))


(defn produce-id! [idset v]
  (swap! idset
         (fn [[id->v v->id]]
           (if (v->id v)
             [id->v v->id]
             (let [id (inc (apply max (concat [0] (keys id->v))))]
               [(assoc id->v id v)
                (assoc v->id v id)]))))
  ((second @idset) v))

(defn find-value [idset id]
  ((first @idset) id))


