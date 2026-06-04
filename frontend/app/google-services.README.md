# google-services.json (placeholder)

Plik `google-services.json` w tym katalogu to **szablon deweloperski** umożliwiający kompilację z pluginem Firebase.

Aby push działał na prawdziwym urządzeniu:

1. Utwórz projekt w [Firebase Console](https://console.firebase.google.com/).
2. Dodaj aplikację Android o `applicationId`: `pl.edu.ur.blokur`.
3. Pobierz prawdziwy `google-services.json` i **zastąp** ten plik.
4. Włącz Cloud Messaging w projekcie Firebase.

Bez prawdziwego projektu Firebase token FCM może być niedostępny — aplikacja wtedy pomija rejestrację urządzenia (bez crasha).
