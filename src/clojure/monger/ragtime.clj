;; Copyright (c) 2011-2014 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.ragtime
  "Ragtime integration"
  (:refer-clojure :exclude [find sort])
  (:require [ragtime.core      :as ragtime]
            [monger.core       :as mg]
            [monger.collection :as mc]
            [monger.query :refer [with-collection find sort]])
  (:import java.util.Date
           [com.mongodb DB WriteConcern]))


(def ^{:const true}
  migrations-collection "meta.migrations")


(extend-type com.mongodb.DB
  ragtime/Migratable
  (add-migration-id [db id]
    (mc/insert db migrations-collection {:_id id :created_at (Date.)} WriteConcern/FSYNC_SAFE))
  (remove-migration-id [db id]
    (mc/remove-by-id db migrations-collection id))
  (applied-migration-ids [db]
    (let [xs (with-collection db migrations-collection
               (find {})
               (sort {:created_at 1}))]
      (vec (map :_id xs)))))


(defn flush-migrations!
  "REMOVES all the information about previously performed migrations"
  [^DB db]
  (mc/remove db migrations-collection))
