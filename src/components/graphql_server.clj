(ns components.graphql-server
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [integrant.core :as ig]
            [com.walmartlabs.lacinia.util :refer [attach-resolvers]]
            [com.walmartlabs.lacinia.pedestal :refer [service-map]]
            [com.walmartlabs.lacinia.schema :as schema]
            [io.pedestal.http :as http]))

(defmethod ig/init-key :graphql-server [_ {:keys [enable-graphiql? domain]}]
  (-> "schema.edn"
      io/resource
      slurp
      edn/read-string
      (attach-resolvers {:get-hero (fn [_ _ _]
                                     {:hello :world})
                         :parties/get-parties (fn [_ _ _] ((get-in domain [:parties :select-all-parties])))
                         :products/get-products (fn [_ _ _] ((get-in domain [:products :select-all-products])))
                         })
      schema/compile
      (service-map {:graphiql enable-graphiql?})
      http/create-server
      http/start))

(defmethod ig/halt-key! :graphql-server [_ server]
  (http/stop server))

