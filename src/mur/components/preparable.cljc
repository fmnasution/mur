(ns mur.components.preparable)

;; =================================================================
;; protocols
;; =================================================================

(defprotocol IPreparable
  (prepare [this]))

(defn preparable?
  [x]
  (satisfies? IPreparable x))
