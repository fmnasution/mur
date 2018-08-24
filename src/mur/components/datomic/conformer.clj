(ns mur.components.datomic.conformer
  (:require
   [clojure.spec.alpha :as s]
   [clojure.java.io :as io]
   [com.stuartsierra.component :as c]
   [io.rkn.conformity :refer [ensure-conforms]]
   [mur.components.datomic :as cptdtm]))

;; =================================================================
;; datomic conformer spec
;; =================================================================

(s/def ::path
  string?)

;; =================================================================
;; datomic conformer
;; =================================================================

(defn- ensure-conforms!
  [conn path]
  (when-let [norm-map (some-> (io/resource path) (slurp) (read-string))]
    (ensure-conforms conn norm-map)))

(defrecord DatomicConformer [datomic-conn path result]
  c/Lifecycle
  (start [this]
    (if (some? result)
      this
      (assoc this :result (ensure-conforms! (cptdtm/conn datomic-conn) path))))
  (stop [this]
    (if (nil? result)
      this
      (assoc this :result nil))))

(defn make-datomic-conformer
  [path]
  (c/using
   (map->DatomicConformer {:path path})
   [:datomic-conn]))
