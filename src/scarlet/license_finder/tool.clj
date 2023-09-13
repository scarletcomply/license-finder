(ns scarlet.license-finder.tool
  (:require
   [scarlet.license-finder.core :as core]
   [scarlet.license-finder.report :as report]))

(defn report
  [{:keys [project]
    :or   {project "./deps.edn"}
    :as options}]
  (->> (core/find-licenses project options)
       (report/generate-report options)))
