addCommandAlias("fullClean", ";project crontab ;clean ;cleanFiles")
addCommandAlias("tc", ";test:compile")
addCommandAlias("cc", ";fullClean   ;test:compile")
addCommandAlias("ctc", ";clean  ;test:compile")
addCommandAlias("ccTest", ";cc   ;test")
addCommandAlias("to", "test-only")

