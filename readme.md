Dit is enkel de src van het project



#Hoe een datastore emuleren?

Doe alle volgende stappen:
https://cloud.google.com/sdk/docs/downloads-interactive#windows

```
cd "C:\Program Files (x86)\Google\Cloud SDK>"
cd  "C:\Users\Steven\AppData\Local\Google\Cloud SDK"

gcloud beta emulators datastore start --host-port 127.0.0.1:8081 --project distributed-systems-gae --no-store-on-disk
```

#Vragen maandag:
1) 2 specials confirmen
2) 3 Eco's confirmen -> Random kan dezelfde auto's aanwijzen
3) Vraag 9: publish/subscribe? Saga?