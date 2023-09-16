(ns scarlet.license-finder.report
  (:require [babashka.fs :as fs]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(def headers ["Dependency" "Version" "Type" "License ID" "License Name" "License URL"])

(defn- dep->vec
  "Return a flat vector with dependency's data out of a nested map"
  [{:keys [version type license] dep-name :name}]
  [dep-name
   version
   (name type)
   (:id license)
   (:name license)
   (:url license)])

(defn- prep-path
  [{:keys [path]}]
  (let [path (-> path
                 fs/path
                 fs/normalize)]
    (when-let [dir (fs/parent path)]
      (fs/create-dirs dir))
    (println "Licenses written to" (str path))
    path))

(defn- prep-writer
  "Get a io/writer for a given path, make parent directories as needed.
  Path value `:stdout` creates a writer with the standard output."
  [{:keys [path] :as options}]
  (cond
    (= path :stdout) {:writer *out* :close? false}
    :else {:writer (-> options
                       prep-path
                       fs/file
                       io/writer)
           :close? true}))

(defn- format-csv [options deps]
  (let [{:keys [writer close?]} (prep-writer options)]
    (->> deps
         (map dep->vec)
         (cons headers)
         (csv/write-csv writer))
    (when close?
      (.close writer))))

;; Support new report formats by adding new defmethods

(defmulti generate-report
  "Generate and write a report for a given format"
  (fn [options _]
    (get options :format)))

(defmethod generate-report :default [options deps]
  (format-csv options deps))

;; Backward compatibility

(defn ^{:deprecated "0.3.0"} write-report
  [{:keys [project out-file format]} deps]
  (let [format (or format :csv)
        path (or out-file
                 (fs/path "./target/licenses" (str project "." (name format))))
        opts {:path path :format format}]
    (format-csv opts deps)))
