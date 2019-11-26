Dit is enkel de src van het project



#Hoe een datastore emuleren?

Start een cmd als adminitstrator --> vervang door u naam

```
cd  "C:\Users\Dries\AppData\Local\Google\ct4j-cloud-sdk\LATEST\google-cloud-sdk\bin"
cd  "C:\Users\Steven\AppData\Local\Google\Cloud SDK"

gcloud beta emulators datastore start --host-port 127.0.0.1:8081 --project distributed-systems-gae --no-store-on-disk
```

--> de eerste keer dingen installeren --> hierna command her opstarten