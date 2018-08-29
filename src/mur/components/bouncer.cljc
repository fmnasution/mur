(ns mur.components.bouncer
  (:require
   [bouncer.core :as bnc]))

;; =================================================================
;; protocols
;; =================================================================

(defprotocol IValidator
  (valid? [this data])
  (validate [this data]))

;; =================================================================
;; validator
;; =================================================================

(defrecord Validator [schema]
  IValidator
  (valid? [this data]
    (bnc/valid? data schema))
  (validate [this data]
    (bnc/validate data schema)))

(defn make-validator
  [schema]
  (->Validator schema))
