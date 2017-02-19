(ns cljdsl.lang
  (:refer-clojure :exclude [alias group] :rename {set core-set})
  (:require
   [clojure.string :as str]
   [cljdsl.model :refer [add! get-nodes]]))



(defn user-type'
  ([id doc fields keywords]
   {:node-type :user-type :id id :doc doc :fields fields :keywords keywords})
  ([id doc fields] (user-type' id doc fields nil)))

(defmacro user-type [id doc fields]
  (list user-type' (list 'quote id) doc (list 'quote fields)))

(defn alias'
    ([id doc type]
     {:node-type :alias
      :id id
      :type-ref type
      :doc doc})
    ([id doc type schemas]
     (let [b (alias' id doc type)]
       (if schemas (assoc b :schemas schemas) b))))


(defmacro alias [id doc type & schemas]
  (list alias' (list 'quote id) doc (list 'quote type) (list 'quote schemas)))


(defn replace-ns
  "Given [user/id tenant] will return tenant/id"
  [id ns]
  (cond
    (keyword? id) (keyword (str ns) (name id))
    (symbol? id) (symbol (str ns) (name id))))

(defn adjust-ns
  "Adjusts id references by inserting ns if missing"
  [id ns]
  (if (namespace id) id (replace-ns id ns)))



(defn resolve-id
  "Given id in 'user' package, will search for user/id, base/id or core/id"
  [type-ref ns nodes]
  (if (= "native" (namespace type-ref))
    {:id type-ref}  
    (let [
          search [ns "base" "core"]
          found (map #(get nodes (adjust-ns type-ref %)) search)
          choice (some identity found)
          ]
      choice)))

(defn unfold-user-type [{:keys [id fields ns] :as node} nodes]
  (assoc node :fields
         (vec (for [f fields]
                (let [choice (resolve-id f ns nodes)]
                  (assert choice (str "Not found ref " f " from user-type " node))
                  (:id choice)
                  )))))

(defn unfold-alias [{:keys [id type-ref ns] :as node} nodes]
  ;; skip complex types for now
  (let [choice (resolve-id type-ref ns nodes)]
    (assert choice (str "Not found ref " type-ref " from alias" node))
    (assoc node :type-ref (:id choice))))

(defn unfold-group-node [node nodes]
  (case (:node-type node)
    :alias (unfold-alias node nodes)
    :user-type (unfold-user-type node nodes)
    node
      ))


(defn link-native- [id doc type]
  {:node-type :native-type
   :id id
   :doc doc
   :type-ref (symbol "native" type)
   :ns "native"})

(defmacro native [id doc type]
  (list link-native- (list 'quote id) doc type))

(defn group' [ns doc nodes]
  (->> nodes
       (map-indexed
        (fn [idx node]
          (-> node
              (update :id adjust-ns ns)
              (assoc :seq idx)
              (assoc :ns ns)
              (unfold-group-node (get-nodes))
              add!)))
       doall
       (mapv :id)
       (assoc {:node-type :group :id ns :doc doc} :nodes)
       add!))

(defn group [ns doc & forms]
  (group' ns doc forms))



