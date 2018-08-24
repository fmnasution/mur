(ns mur.components.bidi
  (:require
   [clojure.spec.alpha :as s]
   [com.stuartsierra.component :as c]
   [bidi.bidi :as b]
   [bidi.ring :refer [make-handler]]
   [mur.components.ring :as cptrng]))

;; =================================================================
;; router spec
;; =================================================================

(s/def ::not-found-handler
  fn?)

;; =================================================================
;; endpoint spec
;; =================================================================

(s/def ::routes
  (s/tuple (s/or :path               string?
                 :parameterized-path (s/coll-of (s/or :part  string?
                                                      :param keyword?)))
           (s/or :endpoint fn?
                 :routes   (s/coll-of ::routes))))

;; =================================================================
;; router
;; =================================================================

(defn- collect-routes
  [component]
  (if-let [map-route (not-empty
                      (into []
                            (comp
                             (map val)
                             (filter #(satisfies? b/RouteProvider %))
                             (map b/routes))
                            component))]
    map-route
    (throw (ex-info "No RouteProvider to be found" {}))))

(defrecord RingRouter [not-found-handler routes]
  c/Lifecycle
  (start [this]
    (if (some? routes)
      this
      (assoc this :routes ["" (collect-routes this)])))
  (stop [this]
    (if (nil? routes)
      this
      (assoc this :routes nil)))

  cptrng/IWebRequestHandler
  (request-handler [this]
    (some-fn (make-handler routes) not-found-handler))

  b/RouteProvider
  (routes [this] routes))

(defn make-ring-router
  [not-found-handler]
  (map->RingRouter
   {:not-found-handler (s/assert ::not-found-handler not-found-handler)}))

;; =================================================================
;; endpoint
;; =================================================================

(defrecord RingEndpoint [routes]
  b/RouteProvider
  (routes [this] routes))

(defn make-ring-endpoint
  [routes]
  (map->RingEndpoint {:routes (s/assert ::routes routes)}))
