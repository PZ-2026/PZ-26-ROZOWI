---

# 1. Generowanie dokumentów PDF 

## 1.1 Kontekst

W backendzie aplikacji został zaimplementowany moduł generowania dokumentów PDF.

Celem zadania było wdrożenie biblioteki do generowania PDF oraz przygotowanie niezależnego serwisu odpowiedzialnego za tworzenie dokumentów na podstawie danych wejściowych.

---

## 1.2 Zastosowane technologie

Moduł został zaimplementowany z wykorzystaniem:

- **Kotlin**
- **Spring Boot**
- **iText 7**

Dodatkowo zastosowano czcionkę **NotoSans** osadzoną w dokumencie, co pozwala na poprawne wyświetlanie polskich znaków.

---

## 1.3 Struktura modułu PDF

```text
backend/
└── src/main/kotlin/pl/edu/ur/blokur/
    ├── controller/
    │   └── PdfController.kt
    ├── service/
    │   └── PdfGeneratorService.kt
    └── dto/
        └── PdfDtos.kt

src/main/resources/
└── fonts/
    └── NotoSans-Regular.ttf
```

Najważniejsze elementy modułu:

- `PdfGeneratorService` – serwis generujący dokument PDF
- `PdfController` – kontroler udostępniający endpoint REST
- `WorkAcceptanceProtocolRequest` – DTO z danymi wejściowymi
- `fonts/` – katalog z czcionką używaną podczas generowania dokumentu

---

## 1.4 Zaimplementowany dokument

W ramach zadania zaimplementowano generowanie dokumentu **Protokół odbioru prac**.

Dokument tworzony jest na podstawie następujących danych:

- numer zgłoszenia
- opis wykonanych prac
- imię i nazwisko konserwatora

Wygenerowany plik PDF zawiera:

- nagłówek dokumentu
- tytuł
- datę wygenerowania
- tabelę z danymi
- miejsce na podpisy

---

## 1.5 Endpoint API

Generowanie dokumentu odbywa się przez endpoint:

```text
POST /api/pdf/work-acceptance-protocol
```

Endpoint przyjmuje dane w formacie JSON i zwraca gotowy plik PDF.

---

## 1.6 Instrukcja testowania

Aby przetestować moduł, należy uruchomić backend aplikacji.

Dla systemów Linux i macOS:

```bash
./gradlew bootRun
```

Dla systemu Windows:

```powershell
.\gradlew.bat bootRun
```

Następnie w nowym oknie PowerShell można wykonać test:

```powershell
$body = @"
{
  "ticketNumber": "ZGL-2026-0042",
  "workDescription": "Wymiana uszkodzonego oświetlenia na klatce schodowej oraz sprawdzenie instalacji elektrycznej.",
  "maintenanceWorkerName": "Jan Kowalski"
}
"@

$bytes = [System.Text.Encoding]::UTF8.GetBytes($body)

Invoke-WebRequest `
    -Uri "http://localhost:8080/api/pdf/work-acceptance-protocol" `
    -Method POST `
    -ContentType "application/json; charset=utf-8" `
    -Body $bytes `
    -OutFile "protokol.pdf"
```

Po wykonaniu polecenia w katalogu pojawi się plik:

```text
protokol.pdf
```

---