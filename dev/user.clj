(ns user
  (:require [scarlet.license-finder.core :as license-finder]))

(comment
  (license-finder/find-licenses "./deps.edn" :transitive? true)
  ;;
  )
