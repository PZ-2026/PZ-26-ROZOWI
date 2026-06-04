# Inwentarz API backendu BlokUR

> Wygenerowano: 2026-06-04 | Spring Boot / Java | JWT + @PreAuthorize
> Role: `ZARZADCA` | `KONSERWATOR` | `MIESZKANIEC`

---

## Legenda

| Symbol | Znaczenie |
|---|---|
| 🔓 | Endpoint publiczny (nie wymaga JWT) |
| 🔐 | Wymaga dowolnego JWT (zalogowany użytkownik) |
| 👑 | Tylko ZARZADCA |
| 🔧 | Tylko KONSERWATOR |
| 🏠 | Tylko MIESZKANIEC |
| 👑🔧 | ZARZADCA lub KONSERWATOR |
| 👑🏠 | ZARZADCA lub MIESZKANIEC |
| 🌐 | Wszyscy zalogowani |

---

## MODUŁ 1 — Uwierzytelnianie i Sesja

**Klasy:** `AuthController` · `InvitationService` · `LoginAttemptService` · `PasswordResetService` · `RefreshTokenService`
**Baza URL:** `/api/auth`
**Security global:** 5 endpointów jest w `permitAll()` w SecurityConfig, reszta wymaga JWT.

### Mechanizmy bezpieczeństwa

| Mechanizm | Opis |
|---|---|
| JWT (access token) | Krótkotrwały token dostępu, przekazywany w nagłówku `Authorization: Bearer <token>` |
| Refresh token | Długotrwały token rotacyjny (każde odświeżenie unieważnia stary) |
| Brute-force lock | Po 3 nieudanych próbach → blokada konta na 15 min (in-memory `ConcurrentHashMap`) |
| Zaproszenie e-mail | Token UUID, TTL 72 h, wysyłany asynchronicznie przez SMTP |
| Reset hasła | Token UUID, TTL 1 h, wysyłany asynchronicznie przez SMTP |
| BCrypt | Koszt 12, wszystkie hasła hashowane |

---

### Endpointy — AuthController

#### 1.1 `POST /api/auth/login`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🔓 Publiczny |
| **Opis** | Logowanie użytkownika. Sprawdza blokadę konta, weryfikuje hasło BCrypt, zwraca parę JWT |

**Request body:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Response `200 OK`:**
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "role": "ZARZADCA | KONSERWATOR | MIESZKANIEC"
}
```

**Błędy:**
- `403` — konto zablokowane (po 3 nieudanych próbach); body: `{ "lockedUntil": "ISO datetime" }`
- `401` — złe hasło / konto nieaktywne; każda nieudana próba rejestrowana przez `LoginAttemptService`

---

#### 1.2 `POST /api/auth/refresh`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🔓 Publiczny |
| **Opis** | Wymiana ważnego refresh tokenu na nową parę (access + refresh). Stary token unieważniany (rotacja). |

**Request body:**
```json
{ "refreshToken": "string" }
```

**Response `200 OK`:**
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "role": "string"
}
```

**Błędy:** `400` — token nieprawidłowy / unieważniony / wygasły

---

#### 1.3 `POST /api/auth/forgot-password`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🔓 Publiczny |
| **Opis** | Inicjuje reset hasła. Generuje token UUID (TTL 1 h) i wysyła e-mail asynchronicznie. Ze względów bezpieczeństwa zawsze zwraca 200, nawet jeśli e-mail nie istnieje. |

**Request body:**
```json
{ "email": "string" }
```

**Response `200 OK`:** brak body

---

#### 1.4 `POST /api/auth/reset-password`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🔓 Publiczny |
| **Opis** | Ustawia nowe hasło na podstawie tokenu z e-maila. Token jednorazowy — po użyciu usuwany. |

**Request body:**
```json
{
  "token": "string",
  "newPassword": "string"
}
```

**Response `200 OK`:** brak body

**Błędy:** `400` — token nieprawidłowy lub wygasły

---

#### 1.5 `POST /api/auth/accept-invitation`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🔓 Publiczny |
| **Opis** | Aktywacja konta przez mieszkańca/konserwatora. Ustawia hasło, usuwa token zaproszenia (TTL 72 h). |

**Request body:**
```json
{
  "token": "string",
  "newPassword": "string"
}
```

**Response `200 OK`:** brak body

**Błędy:**
- `400` — token nieprawidłowy
- `410` — token wygasł (TTL 72 h przekroczony)

---

## MODUŁ 2 — Zarządzanie Użytkownikami

**Klasy:** `AdminUserController` · `UserController` · `AdminUserService` · `UserService`
**Baza URL:** `/api/admin/users`, `/api/users`

---

### Endpointy — AdminUserController

#### 2.1 `GET /api/admin/users`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Lista wszystkich użytkowników (nieusuniętych) z rolą, lokalem i statusem aktywności. |

**Response `200 OK`:**
```json
[
  {
    "id": "UUID",
    "firstName": "string",
    "lastName": "string",
    "email": "string",
    "phone": "string",
    "role": "string",
    "active": true,
    "createdAt": "ISO datetime",
    "apartmentId": "UUID | null"
  }
]
```

---

#### 2.2 `POST /api/admin/users`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Tworzy nowe konto użytkownika z pustym hasłem, przypisuje do lokalu i wysyła e-mail z zaproszeniem (link TTL 72 h). |

**Request body:**
```json
{
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "role": "KONSERWATOR | MIESZKANIEC",
  "apartmentId": "UUID"
}
```

**Response `201 Created`:** obiekt `UserResponse`

**Błędy:** `400` — e-mail zajęty lub lokal nie istnieje

---

#### 2.3 `PUT /api/admin/users/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Aktualizacja danych użytkownika. Jeśli `apartmentId != null` — stare przypisanie do lokalu zastępowane nowym. |

**Request body:**
```json
{
  "firstName": "string",
  "lastName": "string",
  "phone": "string",
  "role": "string",
  "apartmentId": "UUID | null"
}
```

**Response `200 OK`:** obiekt `UserResponse`

---

#### 2.4 `PATCH /api/admin/users/{id}/deactivate`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Dezaktywuje konto (flaga `is_active = false`). Historia zgłoszeń i rozliczeń zachowana. |

**Response `204 No Content`**

---

#### 2.5 `DELETE /api/admin/users/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Usuwa użytkownika (soft delete). |

**Response `204 No Content`**

---

### Endpointy — UserController

#### 2.6 `GET /api/users`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑🔧 ZARZADCA lub KONSERWATOR |
| **Query param** | `role=KONSERWATOR` (wymagany) |
| **Opis** | Zwraca listę użytkowników o podanej roli z liczbą aktywnych przypisanych zgłoszeń. Używane do wyboru konserwatora przy przypisaniu zgłoszenia. |

**Response `200 OK`:**
```json
[
  {
    "id": "UUID",
    "firstName": "string",
    "lastName": "string",
    "activeTicketCount": 5
  }
]
```

---

## MODUŁ 3 — Nieruchomości i Struktura Budynku

**Klasy:** `PropertyController` · `BuildingController` · `StaircaseController` · `PropertyService` · `BuildingService`
**Baza URL:** `/api/properties`, `/api/buildings`, `/api/staircases`

---

### Endpointy — PropertyController

#### 3.1 `GET /api/properties`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Lista wszystkich nieruchomości (wspólnot mieszkaniowych). |

**Response `200 OK`:**
```json
[
  {
    "id": "UUID",
    "name": "string",
    "address": "string",
    "nip": "string",
    "managerPhone": "string",
    "managerEmail": "string",
    "logoPath": "string | null"
  }
]
```

---

#### 3.2 `GET /api/properties/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Szczegóły wskazanej nieruchomości. |

**Response `200 OK`:** obiekt `PropertyResponse`

---

#### 3.3 `POST /api/properties`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Tworzy nową nieruchomość. NIP musi być unikalny. |

**Request body:**
```json
{
  "name": "string",
  "address": "string",
  "nip": "string",
  "managerPhone": "string",
  "managerEmail": "string"
}
```

**Response `201 Created`:** obiekt `PropertyResponse`

**Błędy:** `409` — NIP już istnieje

---

#### 3.4 `PUT /api/properties/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Aktualizacja danych nieruchomości. Logo aktualizowane osobno (endpoint 3.5). |

**Request body:** jak 3.3

**Response `200 OK`:** obiekt `PropertyResponse`

---

#### 3.5 `POST /api/properties/{id}/logo`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Content-Type** | `multipart/form-data` |
| **Opis** | Wgrywa logo nieruchomości na dysk lokalny (`uploads/logos/{id}.png|jpg`). Aktualizuje pole `logo_path`. |

**Form field:** `file` — plik PNG lub JPEG, max 2 MB

**Response `200 OK`:** obiekt `PropertyResponse` z wypełnionym `logoPath`

**Błędy:** `400` — zły typ MIME lub plik > 2 MB

---

### Endpointy — BuildingController

#### 3.6 `GET /api/buildings`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Pełne drzewo hierarchii budynków: budynek → klatki → lokale. |

**Response `200 OK`:**
```json
[
  {
    "id": "UUID",
    "estateName": "string",
    "name": "string",
    "address": "string",
    "latitude": 0.0,
    "longitude": 0.0,
    "staircases": [
      {
        "id": "UUID",
        "label": "string",
        "apartments": [
          {
            "id": "UUID",
            "number": "string",
            "floor": 0,
            "areaM2": 0.0,
            "ownershipType": "string",
            "currentBalance": 0.00
          }
        ]
      }
    ]
  }
]
```

---

#### 3.7 `POST /api/buildings`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Tworzy nowy budynek. Opcjonalne przypisanie do nieruchomości przez `propertyId`. |

**Request body:**
```json
{
  "estateName": "string",
  "name": "string",
  "address": "string",
  "latitude": 0.0,
  "longitude": 0.0,
  "propertyId": "UUID | null"
}
```

**Response `201 Created`:** obiekt `BuildingResponse`

---

#### 3.8 `PUT /api/buildings/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Aktualizacja danych budynku. |

**Request body:** jak 3.7

**Response `200 OK`:** obiekt `BuildingResponse`

---

#### 3.9 `DELETE /api/buildings/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Usuwa budynek. Operacja niedozwolona jeśli budynek ma powiązane lokale. |

**Response `204 No Content`**

**Błędy:** `409` — budynek posiada lokale

---

#### 3.10 `POST /api/buildings/{buildingId}/staircases`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Tworzy nową klatkę schodową w budynku. |

**Request body:**
```json
{ "label": "string" }
```

**Response `201 Created`:**
```json
{ "id": "UUID", "label": "string", "buildingId": "UUID" }
```

---

#### 3.11 `PUT /api/buildings/{buildingId}/staircases/{staircaseId}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Aktualizacja danych klatki schodowej. Weryfikuje przynależność do budynku. |

**Request body:** `{ "label": "string" }`

**Response `200 OK`:** obiekt `StaircaseResponse`

---

#### 3.12 `DELETE /api/buildings/{buildingId}/staircases/{staircaseId}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Usuwa klatkę schodową. Niedozwolone gdy klatka ma lokale. |

**Response `204 No Content`**

---

### Endpointy — StaircaseController

#### 3.13 `POST /api/staircases/{staircaseId}/apartments`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Tworzy nowy lokal w klatce schodowej. |

**Request body:**
```json
{
  "number": "string",
  "floor": 0,
  "areaM2": 0.00,
  "ownershipType": "string"
}
```

**Response `201 Created`:**
```json
{
  "id": "UUID",
  "staircaseId": "UUID",
  "number": "string",
  "floor": 0,
  "areaM2": 0.00,
  "ownershipType": "string",
  "currentBalance": 0.00
}
```

---

#### 3.14 `PUT /api/staircases/{staircaseId}/apartments/{apartmentId}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Aktualizacja danych lokalu. Weryfikuje przynależność do klatki. |

**Request body:** jak 3.13

**Response `200 OK`:** `ApartmentResponse`

---

#### 3.15 `DELETE /api/staircases/{staircaseId}/apartments/{apartmentId}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Usuwa lokal. Historyczne powiązania ze zgłoszeniami pozostają. |

**Response `204 No Content`**

---

## MODUŁ 4 — Zgłoszenia Serwisowe (Tickets)

**Klasy:** `TicketController` · `TicketCommentController` · `TicketImageController` · `CategoryController` · `AdminCategoryController`
**Serwisy:** `TicketService` (827 linii) · `TicketCommentService` · `TicketImageService` · `TicketCategoryService` · `TicketStateMachine`
**Baza URL:** `/api/tickets`, `/api/categories`, `/api/admin/categories`

### Maszyna stanów zgłoszeń (TicketStateMachine)

```
NOWE ──────────────────────────────────────────────────► ODRZUCONE (ZARZADCA)
  │
  └── (ZARZADCA przypisuje) ──────────────────────────► ZAPLANOWANO
                                                              │
                                                 (KONSERWATOR startWork)
                                                              │
                                                        W_REALIZACJI
                                                         /        \
                                             (KONSERWATOR)   (KONSERWATOR)
                                            completeWork()   suspendWork()
                                                  │                │
                                       ZAKONCZONE_DO_WERYFIKACJI  WSTRZYMANO
                                                  │
                                         (ZARZADCA closeTicket)
                                                  │
                                            ZAMKNIETE
```

**Pola TicketDetailDto (response):** `id`, `ticketNumber`, `title`, `description`, `status`, `plannedVisitAt`, `internalNote` *(tylko ZARZADCA/KONSERWATOR)*, `createdAt`, `updatedAt`, `closedAt`, `categoryId`, `categoryName`, `authorId`, `authorName`, `assignedToId`, `assignedToName`, `apartmentId`, `locationLabel`

---

### Endpointy — TicketController

#### 4.1 `POST /api/tickets`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🏠 MIESZKANIEC |
| **Opis** | Tworzy nowe zgłoszenie. Mieszkaniec musi mieć przypisany lokal. Nadawany unikalny numer (format roczny). Status: `NOWE`. |

**Request body:**
```json
{
  "title": "string",
  "description": "string",
  "categoryId": "UUID"
}
```

**Response `201 Created`:** `TicketDetailDto`

**Błędy:** `400` — brak przypisanego lokalu lub kategoria nie istnieje

---

#### 4.2 `GET /api/tickets`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Lista zgłoszeń — widok zależy od roli. ZARZADCA widzi wszystkie z pełnymi filtrami; KONSERWATOR — przypisane do siebie; MIESZKANIEC — dotyczące jego lokalu/klatki/budynku. |

**Query params (opcjonalne):**

| Param | Typ | Opis |
|---|---|---|
| `status` | string | Filtr po statusie (np. `NOWE`) |
| `categoryId` | UUID | Filtr po kategorii |
| `buildingId` | UUID | Tylko ZARZADCA |
| `staircaseId` | UUID | Tylko ZARZADCA |
| `assignedTo` | UUID | Tylko ZARZADCA |
| `dateFrom` | date | Data od |
| `dateTo` | date | Data do |
| `search` | string | Wyszukiwanie tekstowe |

**Response `200 OK`:** `List<TicketSummaryDto>`

---

#### 4.3 `GET /api/tickets/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Szczegóły zgłoszenia. ZARZADCA widzi wszystkie pola; KONSERWATOR — tylko przypisane do siebie; MIESZKANIEC — dotyczące jego lokalu (pole `internalNote` jest ukryte). |

**Response `200 OK`:** `TicketDetailDto`

---

#### 4.4 `POST /api/tickets/{id}/assign`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Przypisuje zgłoszenie do konserwatora. Zmienia status na `ZAPLANOWANO`. Wysyła PUSH do autora zgłoszenia. |

**Request body:**
```json
{
  "assignedTo": "UUID (konserwator)",
  "plannedVisitAt": "ISO datetime",
  "internalNote": "string | null"
}
```

**Response `200 OK`:** `TicketDetailDto`

---

#### 4.5 `POST /api/tickets/{id}/close`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Zamyka zgłoszenie w statusie `ZAKONCZONE_DO_WERYFIKACJI`. Generuje PDF protokołu odbioru, zapisuje do `Document`. Wysyła PUSH (zmiana statusu + nowy dokument). |

**Response `200 OK`:** `TicketDetailDto`

**Efekt uboczny:** tworzy dokument typu `PROTOKOL` w module Dokumentów

---

#### 4.6 `POST /api/tickets/{id}/reject`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Odrzuca zgłoszenie. Powód zapisywany w `internalNote`. Status → `ODRZUCONE`. PUSH do autora. |

**Request body:**
```json
{ "reason": "string" }
```

**Response `200 OK`:** `TicketDetailDto`

---

#### 4.7 `POST /api/tickets/{id}/start-work`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🔧 KONSERWATOR (przypisany do zgłoszenia) |
| **Opis** | Rozpoczyna prace. Status: `ZAPLANOWANO` → `W_REALIZACJI`. PUSH do autora. |

**Response `200 OK`:** `TicketDetailDto`

---

#### 4.8 `POST /api/tickets/{id}/suspend`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🔧 KONSERWATOR (przypisany) |
| **Opis** | Wstrzymuje prace. Status: `W_REALIZACJI` → `WSTRZYMANO`. Powód w `internalNote`. PUSH do wszystkich ZARZADCA. |

**Request body:**
```json
{ "reason": "string" }
```

**Response `200 OK`:** `TicketDetailDto`

---

#### 4.9 `POST /api/tickets/{id}/complete`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🔧 KONSERWATOR (przypisany) |
| **Opis** | Kończy prace. Status: `W_REALIZACJI` → `ZAKONCZONE_DO_WERYFIKACJI`. Zapisuje opis wykonanych prac. PUSH do zarządców. |

**Request body:**
```json
{ "workDescription": "string" }
```

**Response `200 OK`:** `TicketDetailDto`

---

#### 4.10 `PATCH /api/tickets/{id}/status`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑🔧 ZARZADCA lub KONSERWATOR |
| **Opis** | Generyczny endpoint zmiany statusu walidowany przez `TicketStateMachine`. KONSERWATOR może zmieniać tylko własne zgłoszenia. |

**Request body:**
```json
{
  "status": "TicketStatus enum value",
  "comment": "string | null"
}
```

**Response `200 OK`:** `TicketDetailDto`

---

#### 4.11 `GET /api/tickets/{id}/history`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑🔧 ZARZADCA lub KONSERWATOR |
| **Opis** | Historia zmian statusu zgłoszenia. |

**Response `200 OK`:**
```json
[
  {
    "status": "string",
    "changedBy": "string (imię nazwisko)",
    "comment": "string | null",
    "createdAt": "ISO datetime"
  }
]
```

---

### Endpointy — TicketCommentController

#### 4.12 `POST /api/tickets/{ticketId}/comments`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Dodaje komentarz. MIESZKANIEC — tylko `PUBLICZNY` (do własnych zgłoszeń). KONSERWATOR — tylko `WEWNETRZNY` (do przypisanych). ZARZADCA — oba typy. |

**Request body:**
```json
{
  "content": "string",
  "commentType": "PUBLICZNY | WEWNETRZNY"
}
```

**Response `201 Created`:**
```json
{
  "id": "UUID",
  "ticketId": "UUID",
  "authorName": "string",
  "content": "string",
  "commentType": "PUBLICZNY | WEWNETRZNY",
  "createdAt": "ISO datetime"
}
```

---

#### 4.13 `GET /api/tickets/{ticketId}/comments`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Lista komentarzy. MIESZKANIEC widzi tylko `PUBLICZNY`. KONSERWATOR i ZARZADCA widzą oba typy. |

**Response `200 OK`:** `List<TicketCommentDto>`

---

### Endpointy — TicketImageController

#### 4.14 `POST /api/tickets/{ticketId}/images`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑🔧 ZARZADCA lub KONSERWATOR |
| **Content-Type** | `multipart/form-data` |
| **Opis** | Wgrywa zdjęcie do zgłoszenia. Typ obrazu: `BEFORE` (przed pracami) lub `AFTER` (po). Walidacja MIME (JPEG/PNG/WEBP). |

**Form fields:**
- `file` — plik graficzny
- `imageType` — `BEFORE | AFTER`

**Response `201 Created`:**
```json
{
  "id": "UUID",
  "ticketId": "UUID",
  "imageType": "BEFORE | AFTER",
  "filePath": "string",
  "uploadedAt": "ISO datetime"
}
```

---

#### 4.15 `GET /api/tickets/{ticketId}/images`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Lista metadanych obrazów dla zgłoszenia. |

**Response `200 OK`:** `List<TicketImageDto>`

---

#### 4.16 `GET /api/images/{imageId}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Pobiera plik obrazu jako stream binarny (`image/jpeg` lub `image/png`). |

**Response `200 OK`:** `Resource` (plik binarny)

---

#### 4.17 `DELETE /api/images/{imageId}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Usuwa obraz z dysku i z bazy danych. |

**Response `204 No Content`**

---

### Endpointy — CategoryController

#### 4.18 `GET /api/categories`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🔓 Publiczny (w `SecurityConfig.permitAll()`) |
| **Opis** | Lista aktywnych kategorii zgłoszeń (niezdeaktywowanych). |

**Response `200 OK`:**
```json
[
  {
    "id": "UUID",
    "name": "string",
    "slaHours": 0
  }
]
```

---

### Endpointy — AdminCategoryController

#### 4.19 `GET /api/admin/categories`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Lista wszystkich kategorii (aktywnych i nieaktywnych). |

**Response `200 OK`:** `List<CategoryResponse>`

---

#### 4.20 `POST /api/admin/categories`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Tworzy nową kategorię zgłoszeń. |

**Request body:** `{ "name": "string" }`

**Response `201 Created`:** `CategoryResponse`

---

#### 4.21 `PUT /api/admin/categories/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Zmiana nazwy kategorii. |

**Request body:** `{ "name": "string" }`

**Response `200 OK`:** `CategoryResponse`

---

#### 4.22 `PATCH /api/admin/categories/{id}/sla`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Ustawia docelowy czas SLA (godziny robocze) dla kategorii. |

**Request body:** `{ "slaHours": 48 }`

**Response `204 No Content`**

---

#### 4.23 `DELETE /api/admin/categories/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Deaktywuje kategorię (soft delete — `is_active = false`). |

**Response `204 No Content`**

---

## MODUŁ 5 — Ogłoszenia

**Klasy:** `AnnouncementController` · `AnnouncementService`
**Baza URL:** `/api/announcements`

### Typy zasięgu ogłoszeń (`AnnouncementTargetType`)

| Typ | Opis |
|---|---|
| `WSZYSCY` | Wszyscy mieszkańcy systemu |
| `BUDYNEK` | Mieszkańcy konkretnego budynku (wymagany `targetId`) |
| `KLATKA` | Mieszkańcy konkretnej klatki (wymagany `targetId`) |
| `NIERUCHOMOSC` | Mieszkańcy konkretnego lokalu (wymagany `targetId`) |

Po zapisaniu ogłoszenia wysyłane są asynchroniczne powiadomienia PUSH (FCM) do adresatów.
Ogłoszenia starsze niż **12 miesięcy** nie są zwracane dla użytkowników.

---

### Endpointy — AnnouncementController

#### 5.1 `GET /api/announcements`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Lista ogłoszeń dopasowanych do zalogowanego użytkownika. Filtrowane wg lokalu/klatki/budynku z ostatnich 12 mies. |

**Response `200 OK`:**
```json
[
  {
    "id": "UUID",
    "type": "string",
    "title": "string",
    "content": "string",
    "authorName": "string",
    "targetType": "WSZYSCY | BUDYNEK | KLATKA | NIERUCHOMOSC",
    "attachmentUrl": "/api/announcements/{id}/attachment | null",
    "plannedDate": "date | null",
    "createdAt": "ISO datetime"
  }
]
```

---

#### 5.2 `POST /api/announcements`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA (weryfikacja w serwisie) |
| **Content-Type** | `multipart/form-data` |
| **Opis** | Tworzy ogłoszenie z opcjonalnym załącznikiem PDF. Po zapisie wysyłane są asynchroniczne PUSH FCM do adresatów. |

**Form fields:**
- `announcement` — JSON: `{ "title", "content", "targetType", "targetId": "UUID|null", "plannedDate": "date|null" }`
- `attachment` — plik PDF (opcjonalny)

**Response `201 Created`:** `AnnouncementDto`

---

#### 5.3 `PUT /api/announcements/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Content-Type** | `multipart/form-data` |
| **Opis** | Aktualizacja ogłoszenia. Nowy załącznik zastępuje stary. |

**Form fields:** jak 5.2

**Response `200 OK`:** `AnnouncementDto`

---

#### 5.4 `DELETE /api/announcements/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Usuwa ogłoszenie. |

**Response `204 No Content`**

---

#### 5.5 `GET /api/announcements/{id}/attachment`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Pobiera załącznik PDF ogłoszenia jako stream (`application/pdf`). |

**Response `200 OK`:** plik binarny PDF

---

## MODUŁ 6 — Uchwały i Głosowania

**Klasy:** `ResolutionController` · `ResolutionService`
**Baza URL:** `/api/resolutions`

### Logika dostępu do wyników głosowania

Wyniki (liczby głosów per opcja) widoczne tylko gdy:
1. Pytający jest ZARZADCA
2. Data zakończenia głosowania (`endDate`) minęła
3. Pytający użytkownik już oddał głos

Przed oddaniem głosu — widoczna jest jedynie lista opcji.

---

### Endpointy — ResolutionController

#### 6.1 `GET /api/resolutions`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Lista uchwał. ZARZADCA widzi wszystkie; MIESZKANIEC widzi uchwały powiązane z jego budynkiem. |

**Response `200 OK`:**
```json
[
  {
    "id": "UUID",
    "title": "string",
    "description": "string",
    "endDate": "ISO datetime",
    "targetBuildingId": "UUID",
    "authorName": "string"
  }
]
```

---

#### 6.2 `GET /api/resolutions/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Szczegóły uchwały z opcjami głosowania. Wyniki dołączane warunkowo (patrz logika dostępu powyżej). MIESZKANIEC musi należeć do budynku uchwały. |

**Response `200 OK`:**
```json
{
  "id": "UUID",
  "title": "string",
  "description": "string",
  "endDate": "ISO datetime",
  "targetBuildingId": "UUID",
  "authorName": "string",
  "options": [
    { "id": "UUID", "optionText": "string" }
  ],
  "results": [
    { "id": "UUID", "optionText": "string", "voteCount": 0 }
  ]
}
```

*(pole `results` = `null` jeśli użytkownik nie może jeszcze zobaczyć wyników)*

---

#### 6.3 `POST /api/resolutions`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA (weryfikacja w serwisie) |
| **Opis** | Tworzy nową uchwałę z listą opcji głosowania. |

**Request body:**
```json
{
  "title": "string",
  "description": "string",
  "endDate": "ISO datetime",
  "targetBuildingId": "UUID",
  "options": ["string", "string"]
}
```

**Response `201 Created`:** brak body

---

#### 6.4 `POST /api/resolutions/{id}/vote`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Oddanie głosu na wybraną opcję. Zabezpieczenie przed podwójnym głosowaniem (unique constraint w bazie → HTTP 409). |

**Request body:**
```json
{ "optionId": "UUID" }
```

**Response `204 No Content`**

**Błędy:**
- `404` — uchwała lub opcja nie istnieje
- `400` — opcja nie należy do tej uchwały
- `409` — użytkownik już głosował

---

#### 6.5 `GET /api/resolutions/{id}/report`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA (weryfikacja w serwisie) |
| **Opis** | Generuje raport PDF z wynikami głosowania (tabela opcji + liczba głosów). Dostępne tylko po zakończeniu głosowania (`endDate` w przeszłości). |

**Response `200 OK`:**
- `Content-Type: application/pdf`
- Plik binarny PDF z raportem

**Błędy:** `400` — głosowanie jeszcze trwa
---

## MODUŁ 7 — Liczniki i Odczyty Liczników

**Klasy:** `MeterController` · `MeterReadingController` · `MeterService` · `MeterReadingService`
**Baza URL:** `/api/apartments/{apartmentId}/meters`, `/api/apartments/{apartmentId}/meter-readings`, `/api/meter-readings`

### Typy mediów (`MediumType`)
`WODA_ZIMNA` | `WODA_CIEPLA` | `CIEPLO` | `GAZ` | `ENERGIA_ELEKTRYCZNA`

### Reguły biznesowe odczytów
- Nie można dodać odczytu z **cofającą się wartością** względem ostatniego (walidacja regresji)
- Nie można dodać **duplikatu** (ten sam licznik + ta sama data)
- Licznik musi być **aktywny** i **przypisany do danego lokalu**
- KONSERWATOR może dodawać odczyty **tylko** dla lokali z przypisanym mu zgłoszeniem
- MIESZKANIEC może pobierać odczyty **tylko** swojego lokalu
- Odczyty soft-delete (flaga `deleted`)

---

### Endpointy — MeterController

#### 7.1 `GET /api/apartments/{apartmentId}/meters`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Lista liczników (aktywnych i nieaktywnych) przypisanych do lokalu. |

**Response `200 OK`:**
```json
[
  {
    "id": "UUID",
    "apartmentId": "UUID",
    "serialNumber": "string",
    "mediumType": "MediumType enum",
    "installationDate": "date",
    "active": true
  }
]
```

---

#### 7.2 `POST /api/apartments/{apartmentId}/meters`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Dodaje nowy licznik do lokalu. Numer seryjny musi być globalnie unikalny. Licznik tworzony jako aktywny. |

**Request body:**
```json
{
  "serialNumber": "string",
  "mediumType": "MediumType enum",
  "installationDate": "date"
}
```

**Response `201 Created`:** `MeterResponse`

**Błędy:** `409` — numer seryjny już istnieje

---

#### 7.3 `PATCH /api/apartments/{apartmentId}/meters/{meterId}/deactivate`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Dezaktywuje licznik (`is_active = false`). Historia odczytów zachowana. |

**Response `200 OK`:** `MeterResponse`

**Błędy:** `400` — licznik już nieaktywny

---

### Endpointy — MeterReadingController

#### 7.4 `GET /api/apartments/{apartmentId}/meter-readings`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑🔧🏠 Wszyscy zalogowani |
| **Opis** | Stronicowana lista odczytów dla lokalu, sortowana malejąco po dacie. MIESZKANIEC widzi tylko odczyty swojego lokalu (row-level security). |

**Query params:**

| Param | Domyślnie | Opis |
|---|---|---|
| `page` | `0` | Numer strony |
| `size` | `20` | Rozmiar strony |

**Response `200 OK`:** strona `Page<MeterReadingResponse>`

```json
{
  "content": [
    {
      "id": "UUID",
      "apartmentId": "UUID",
      "meterId": "UUID",
      "serialNumber": "string",
      "mediumType": "string",
      "value": "BigDecimal",
      "readingDate": "date",
      "createdAt": "ISO datetime",
      "updatedAt": "ISO datetime",
      "recordedBy": "string (email)"
    }
  ],
  "totalElements": 0,
  "totalPages": 0,
  "number": 0,
  "size": 20
}
```

---

#### 7.5 `POST /api/apartments/{apartmentId}/meter-readings`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑🔧🏠 Wszyscy zalogowani |
| **Opis** | Dodaje nowy odczyt licznika. Pełna walidacja reguł biznesowych (patrz wyżej). |

**Request body:**
```json
{
  "meterId": "UUID",
  "value": "BigDecimal (np. 1234.56)",
  "readingDate": "date"
}
```

**Response `201 Created`:** `MeterReadingResponse`

---

#### 7.6 `GET /api/meter-readings/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Szczegóły odczytu z weryfikacją uprawnień: MIESZKANIEC — tylko własny lokal; KONSERWATOR — tylko lokale z przypisanym zgłoszeniem. |

**Response `200 OK`:** `MeterReadingResponse`

---

#### 7.7 `PUT /api/meter-readings/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Aktualizuje odczyt. Rewalidacja duplikatów i regresji z pominięciem edytowanego rekordu. |

**Request body:** jak 7.5

**Response `200 OK`:** `MeterReadingResponse`

---

#### 7.8 `DELETE /api/meter-readings/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Soft-delete odczytu (flaga `deleted = true`). |

**Response `204 No Content`**

---

## MODUŁ 8 — Finanse i Transakcje

**Klasy:** `FinancialTransactionController` · `AdminFinanceController` · `FinancialTransactionService` · `ApartmentBalanceService`
**Baza URL:** `/api/apartments/{id}/transactions`, `/api/finance/import`, `/api/admin/apartments/balances`

### Typy transakcji
`WPLATA` | `NALICZENIE` | `KOREKTA`

### Mechanizm sald
Saldo lokalu (`current_balance`) jest **denormalizowane** — aktualizowane atomowo przy każdej transakcji w ramach jednej transakcji bazodanowej (`@Transactional`).

---

### Endpointy — FinancialTransactionController

#### 8.1 `GET /api/apartments/{apartmentId}/transactions`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑🏠 ZARZADCA lub MIESZKANIEC |
| **Opis** | Historia transakcji finansowych lokalu + aktualne saldo z pola denormalizowanego. |

**Response `200 OK`:**
```json
{
  "currentBalance": "BigDecimal",
  "transactions": [
    {
      "id": "UUID",
      "apartmentId": "UUID",
      "type": "WPLATA | NALICZENIE | KOREKTA",
      "amount": "BigDecimal",
      "description": "string",
      "transactionDate": "date",
      "recordedBy": "string (email)"
    }
  ]
}
```

---

#### 8.2 `POST /api/apartments/{apartmentId}/transactions`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Tworzy nową transakcję finansową. Atomowo aktualizuje `current_balance` lokalu (`balance += amount`; ujemna kwota = obciążenie). |

**Request body:**
```json
{
  "type": "WPLATA | NALICZENIE | KOREKTA",
  "amount": "BigDecimal",
  "description": "string",
  "transactionDate": "date"
}
```

**Response `201 Created`:** `FinancialTransactionResponse`

---

#### 8.3 `POST /api/finance/import`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Content-Type** | `multipart/form-data` |
| **Opis** | Masowy import transakcji z pliku CSV. Przetwarzany wiersz po wierszu — błędy zbierane i raportowane bez przerywania importu. |

**Form field:** `file` — plik CSV

**Format CSV (nagłówek wymagany):**
```
apartment_id,date,type,amount,description
UUID,2024-01-15,WPLATA,500.00,Czynsz styczeń
```

**Response `200 OK`:**
```json
{
  "importedCount": 10,
  "errorCount": 2,
  "errors": [
    { "lineNumber": 3, "message": "Lokal o podanym ID nie istnieje" }
  ]
}
```

---

### Endpointy — AdminFinanceController

#### 8.4 `GET /api/admin/apartments/balances`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Zestawienie sald i zaległości wszystkich lokali. Sortowanie domyślne: malejąco po kwocie zadłużenia. |

**Query params (wszystkie opcjonalne):**

| Param | Typ | Opis |
|---|---|---|
| `propertyId` | UUID | Filtr do konkretnej nieruchomości |
| `minDebt` | BigDecimal | Min. kwota zaległości w PLN (saldo ≤ -minDebt) |
| `minDaysOverdue` | Long | Min. dni od ostatniej wpłaty |
| `sortDesc` | boolean | `true` (domyślnie) = malejąco po zadłużeniu |

**Response `200 OK`:**
```json
[
  {
    "id": "UUID",
    "address": "string (ul. ... m. X)",
    "balance": "BigDecimal",
    "lastPaymentDate": "date | null",
    "daysOverdue": 0
  }
]
```

---

## MODUŁ 9 — Dokumenty

**Klasy:** `DocumentController` · `AdminDocumentController` · `DocumentService` · `DocumentDistributionService`
**Baza URL:** `/api/documents`, `/api/admin/documents`
**Storage:** LocalDisk (`uploads/documents/`) lub S3 (konfigurowalne przez `DocumentStorageConfig`)

### Typy dokumentów (pole `type`)
| Typ | Źródło |
|---|---|
| `PROTOKOL` | Auto-generowany przy zamknięciu zgłoszenia |
| `RAPORT_SALD` | Generowany przez `PdfController` |
| `ZAWIADOMIENIE_STAWKI` | Masowa dystrybucja przez `AdminDocumentController` |
| `ROZLICZENIE_ROCZNE` | Masowa dystrybucja przez `AdminDocumentController` |
| `UCHWALA` | Generowany z modułu Uchwał |

---

### Endpointy — DocumentController

#### 9.1 `GET /api/documents`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑🏠 ZARZADCA lub MIESZKANIEC |
| **Opis** | Lista dokumentów z kontrolą dostępu. ZARZADCA widzi wszystkie z pełnymi filtrami. MIESZKANIEC widzi dokumenty powiązane z jego kontem lub lokalem. |

**Query params (opcjonalne):**

| Param | Typ | Opis |
|---|---|---|
| `apartmentId` | UUID | Filtr po lokalu (MIESZKANIEC — musi być jego lokal) |
| `type` | string | Filtr po typie dokumentu |
| `startDate` | date | Początek zakresu dat |
| `endDate` | date | Koniec zakresu dat |

**Response `200 OK`:**
```json
[
  {
    "id": "UUID",
    "type": "string",
    "title": "string",
    "createdAt": "ISO datetime",
    "downloadUrl": "https://.../api/documents/{id}/download"
  }
]
```

---

#### 9.2 `GET /api/documents/{id}/download`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑🏠 ZARZADCA lub właściciel dokumentu |
| **Opis** | Pobiera plik PDF z DocumentStorage (LocalDisk lub S3). MIESZKANIEC tylko jeśli jest właścicielem (`ownerUserId`) lub dokumenty powiązane z jego lokalem. |

**Response `200 OK`:** plik binarny PDF (`Content-Type: application/pdf`, `Content-Disposition: attachment`)

**Błędy:** `403` — brak dostępu do dokumentu

---

### Endpointy — AdminDocumentController

#### 9.3 `POST /api/admin/documents/rate-change`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Masowa dystrybucja zawiadomień o zmianie stawek. Generuje PDF (szablon `RateChangeNotificationTemplate`) i tworzy wpis Document dla każdego aktywnego mieszkańca w zakresie. Asynchroniczne PUSH FCM. |

**Request body:**
```json
{
  "subject": "string (tytuł zawiadomienia)",
  "body": "string (treść)",
  "effectiveDate": "string (data wejścia w życie)",
  "scope": "ALL | BUILDING | APARTMENT",
  "targetId": "UUID | null (ID budynku lub lokalu, wymagany jeśli scope != ALL)"
}
```

**Response `200 OK`:**
```json
{
  "documentCount": 25,
  "notificationCount": 25,
  "message": "Zawiadomienie wygenerowane i wysłane do 25 lokali."
}
```

---

#### 9.4 `POST /api/admin/documents/annual-settlement`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Masowe generowanie rocznych rozliczeń kosztów lokali. Dla każdego lokalu oblicza saldo otwarcia (transakcje przed rokiem), transakcje w roku i saldo zamknięcia. PDF per lokal per mieszkaniec. |

**Request body:**
```json
{
  "year": 2024,
  "note": "string | null",
  "scope": "ALL | BUILDING | APARTMENT",
  "targetId": "UUID | null"
}
```

**Response `200 OK`:** `DocumentDistributionResult` (jak 9.3)
---

## MODUŁ 10 — PDF (Generowanie)

**Klasy:** `PdfController` · `PdfGeneratorService`
**Baza URL:** `/api/pdf`
**Lib:** iText 7 (`pdf-lib` module)

### Szablony PDF dostępne w systemie

| Szablon | Klasa | Opis |
|---|---|---|
| `WorkAcceptanceProtocol` | `WorkAcceptanceProtocolTemplate` | Protokół odbioru prac — zdjęcia BEFORE/AFTER |
| `BalancesReport` | `BalancesReportTemplate` | Raport sald i zaległości lokali |
| `RateChangeNotification` | `RateChangeNotificationTemplate` | Zawiadomienie o zmianie stawek |
| `AnnualSettlement` | `AnnualSettlementTemplate` | Roczne rozliczenie kosztów lokalu |

---

### Endpointy — PdfController

#### 10.1 `POST /api/pdf/work-acceptance-protocol`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Generuje protokół odbioru prac jako PDF na żądanie (np. do ponownego pobrania). Ścieżki do zdjęć BEFORE/AFTER muszą być dostępne na dysku serwera. |

**Request body:**
```json
{
  "ticketNumber": "string",
  "workDescription": "string",
  "maintenanceWorkerName": "string",
  "beforeImagesPaths": ["string"],
  "afterImagesPaths": ["string"]
}
```

**Response `200 OK`:**
- `Content-Type: application/pdf`
- `Content-Disposition: attachment; filename="protocol-{ticketNumber}.pdf"`

---

#### 10.2 `GET /api/pdf/balances-report`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Generuje raport sald lokali jako PDF. Stosuje te same filtry co `GET /api/admin/apartments/balances`. Opcjonalnie zapisuje dokument do archiwum (`DocumentService`). |

**Query params (opcjonalne):**

| Param | Typ | Opis |
|---|---|---|
| `propertyId` | UUID | Filtr do nieruchomości |
| `minDebt` | BigDecimal | Min. kwota zaległości |
| `minDaysOverdue` | Long | Min. dni zalegania |
| `sortDesc` | boolean | Sortowanie (domyślnie `true`) |
| `archive` | boolean | Jeśli `true` — PDF zapisywany do `documents` (domyślnie `false`) |

**Response `200 OK`:**
- `Content-Type: application/pdf`
- `Content-Disposition: attachment; filename="balances-report.pdf"`

---

## MODUŁ 11 — Przeglądy Techniczne

**Klasy:** `InspectionController` · `InspectionService` · `InspectionReminderJob`
**Baza URL:** `/api/inspections`

### Typy zasięgu przeglądu (`ScopeType`)
`NIERUCHOMOSC` | `BUDYNEK` | `KLATKA`

### Scheduler — InspectionReminderJob
- Automatyczne sprawdzanie nadchodzących przeglądów
- Wysyłanie PUSH FCM (`EVENT_PRZEGLAD`) na określony czas przed datą przeglądu
- Harmonogram konfigurowalny przez `@Scheduled`

### Logika widoczności
- **ZARZADCA:** widzi wszystkie przeglądy
- **MIESZKANIEC/KONSERWATOR:** widzi przeglądy pasujące do jego hierarchii lokalu (klatka → budynek → nieruchomość)

---

### Endpointy — InspectionController

#### 11.1 `GET /api/inspections`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Lista przeglądów technicznych filtrowana wg zasięgu zalogowanego użytkownika. Sortowanie: rosnąco po `scheduledAt`. |

**Response `200 OK`:**
```json
[
  {
    "id": "UUID",
    "title": "string",
    "description": "string",
    "scheduledAt": "ISO datetime",
    "scopeType": "NIERUCHOMOSC | BUDYNEK | KLATKA",
    "scopeId": "UUID",
    "createdByName": "string",
    "createdAt": "ISO datetime"
  }
]
```

---

#### 11.2 `POST /api/inspections`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Tworzy nowy przegląd techniczny. Waliduje istnienie encji wskazanej przez `scopeType` + `scopeId`. |

**Request body:**
```json
{
  "title": "string",
  "description": "string",
  "scheduledAt": "ISO datetime",
  "scopeType": "NIERUCHOMOSC | BUDYNEK | KLATKA",
  "scopeId": "UUID"
}
```

**Response `201 Created`:** `InspectionResponse`

---

#### 11.3 `PUT /api/inspections/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Aktualizacja przeglądu technicznego. Rewalidacja zasięgu. |

**Request body:** jak 11.2

**Response `200 OK`:** `InspectionResponse`

---

#### 11.4 `DELETE /api/inspections/{id}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Usuwa przegląd techniczny (hard delete). |

**Response `204 No Content`**

---

## MODUŁ 12 — Powiadomienia Push i Urządzenia

**Klasy:** `DeviceController` · `AdminNotificationController` · `UserDeviceService` · `NotificationConfigService` · `PushNotificationService`
**Baza URL:** `/api/devices`, `/api/admin/notifications/settings`

### Typy zdarzeń powiadomień PUSH

| EventType | Opis |
|---|---|
| `OGLOSZENIE` | Nowe ogłoszenie |
| `ZMIANA_STATUSU_ZGLOSZENIA` | Zmiana statusu zgłoszenia |
| `PRZEGLAD` | Nadchodzący przegląd techniczny |
| `NOWY_DOKUMENT` | Nowy dokument dostępny |
| `WSTRZYMANIE_ZGLOSZENIA` | Wstrzymanie zgłoszenia (tylko zarządcy) |

### Hierarchia konfiguracji
1. **Globalna** (`notification_config`) — zarządca może wyłączyć cały typ zdarzenia
2. **Per-użytkownik** (`notification_settings`) — użytkownik może wyłączyć konkretny typ dla siebie

Jeśli typ wyłączony globalnie → żaden użytkownik nie otrzyma powiadomienia.

---

### Endpointy — DeviceController

#### 12.1 `POST /api/devices`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Rejestruje token FCM urządzenia dla zalogowanego użytkownika. Operacja idempotentna. Jeśli token należał do innego użytkownika (np. po przelogowaniu) — stary wpis jest usuwany. |

**Request body:**
```json
{
  "fcmToken": "string",
  "platform": "ANDROID | IOS"
}
```

**Response `201 Created`:** brak body

---

#### 12.2 `DELETE /api/devices`

| Pole | Wartość |
|---|---|
| **Dostęp** | 🌐 Wszyscy zalogowani |
| **Opis** | Wyrejestrowanie tokenu FCM (np. przy wylogowaniu). |

**Request body:**
```json
{ "fcmToken": "string" }
```

**Response `204 No Content`**

---

### Endpointy — AdminNotificationController

#### 12.3 `GET /api/admin/notifications/settings`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Pobiera globalną konfigurację wszystkich typów powiadomień PUSH. |

**Response `200 OK`:**
```json
[
  {
    "eventType": "OGLOSZENIE",
    "enabled": true,
    "label": "Ogłoszenia"
  },
  {
    "eventType": "ZMIANA_STATUSU_ZGLOSZENIA",
    "enabled": true,
    "label": "Zmiana statusu zgłoszenia"
  },
  {
    "eventType": "PRZEGLAD",
    "enabled": true,
    "label": "Przeglądy techniczne"
  },
  {
    "eventType": "NOWY_DOKUMENT",
    "enabled": true,
    "label": "Nowe dokumenty"
  },
  {
    "eventType": "WSTRZYMANIE_ZGLOSZENIA",
    "enabled": true,
    "label": "Wstrzymanie zgłoszenia"
  }
]
```

---

#### 12.4 `PATCH /api/admin/notifications/settings/{eventType}`

| Pole | Wartość |
|---|---|
| **Dostęp** | 👑 ZARZADCA |
| **Opis** | Włącza lub wyłącza globalnie dany typ powiadomień PUSH. |

**Request body:**
```json
{ "enabled": true }
```

**Response `200 OK`:** `NotificationConfigResponse`

---

## FAZA 2 — Statystyki i weryfikacja kompletności

### Podsumowanie endpointów per moduł

| # | Moduł | Endpointów |
|---|---|---|
| 1 | Uwierzytelnianie i Sesja | 5 |
| 2 | Zarządzanie Użytkownikami | 6 |
| 3 | Nieruchomości i Struktura Budynku | 15 |
| 4 | Zgłoszenia Serwisowe | 23 |
| 5 | Ogłoszenia | 5 |
| 6 | Uchwały i Głosowania | 5 |
| 7 | Liczniki i Odczyty | 8 |
| 8 | Finanse i Transakcje | 4 |
| 9 | Dokumenty | 4 |
| 10 | PDF | 2 |
| 11 | Przeglądy Techniczne | 4 |
| 12 | Powiadomienia i Urządzenia | 4 |
| **RAZEM** | | **85** |

---

### Podsumowanie endpointów per rola

| Rola | Endpointów |
|---|---|
| 🔓 Publiczny (bez JWT) | 5 |
| 👑 Tylko ZARZADCA | 43 |
| 🔧 Tylko KONSERWATOR | 0 (KONSERWATOR zawsze współdzieli z kimś) |
| 🏠 Tylko MIESZKANIEC | 1 |
| 👑🔧 ZARZADCA lub KONSERWATOR | 4 |
| 👑🏠 ZARZADCA lub MIESZKANIEC | 4 |
| 🌐 Wszyscy zalogowani | 28 |

---

### Lista kontrolerów — weryfikacja pokrycia

| Kontroler | Zainwentaryzowany | Moduł |
|---|---|---|
| `AuthController` | ✅ | 1 |
| `AdminUserController` | ✅ | 2 |
| `UserController` | ✅ | 2 |
| `PropertyController` | ✅ | 3 |
| `BuildingController` | ✅ | 3 |
| `StaircaseController` | ✅ | 3 |
| `TicketController` | ✅ | 4 |
| `TicketCommentController` | ✅ | 4 |
| `TicketImageController` | ✅ | 4 |
| `CategoryController` | ✅ | 4 |
| `AdminCategoryController` | ✅ | 4 |
| `AnnouncementController` | ✅ | 5 |
| `ResolutionController` | ✅ | 6 |
| `MeterController` | ✅ | 7 |
| `MeterReadingController` | ✅ | 7 |
| `FinancialTransactionController` | ✅ | 8 |
| `AdminFinanceController` | ✅ | 8 |
| `DocumentController` | ✅ | 9 |
| `AdminDocumentController` | ✅ | 9 |
| `PdfController` | ✅ | 10 |
| `InspectionController` | ✅ | 11 |
| `AdminNotificationController` | ✅ | 12 |
| `DeviceController` | ✅ | 12 |

**Wynik: 23/23 kontrolerów zainwentaryzowanych — pokrycie 100%** ✅

---

### Kluczowe uwagi architektoniczne

1. **JWT Stateless** — brak sesji serwerowej, całość w nagłówku `Authorization: Bearer`
2. **Rotacja tokenów** — refresh token jednorazowy; nowa para przy każdym odświeżeniu
3. **Brute-force in-memory** — blokada po 3 próbach; nie przetrwa restartu serwera (brak Redis)
4. **Row-Level Security w serwisach** — Spring Security definiuje ramy, szczegółowa walidacja (kto widzi co) realizowana w kodzie serwisu przez `Principal`
5. **Soft-delete** — użytkownicy (`deleted`), odczyty liczników (`deleted`), kategorie (`is_active`) — nie są usuwane fizycznie
6. **DocumentStorage abstraction** — interfejs `DocumentStorage` z dwoma implementacjami: `LocalDiskDocumentStorage` i `S3DocumentStorage` (przełączane przez konfigurację)
7. **Firebase FCM** — powiadomienia PUSH asynchroniczne (`@Async`); nieistniejące tokeny automatycznie usuwane (`UNREGISTERED` error)
8. **SLA** — kalkulator godzin roboczych (`BusinessHoursCalculator`) w module zgłoszeń
9. **Flyway** — migracje schematu bazy danych wersjonowane
10. **CSV import** — masowy import transakcji z walidacją per-wiersz, błędy raportowane bez przerywania całości
# Inwentarz serwisów backendu BlokUR

> Uzupełnienie do plików 01–04 | FAZA 1 punkt 3 — lista publicznych metod serwisów

---

## MODUŁ 1 — Serwisy: Uwierzytelnianie i Sesja

### `InvitationService`

| Metoda | Opis |
|---|---|
| `inviteUser(User user)` | Generuje token UUID (TTL 72 h), zapisuje w `InvitationToken`, wysyła e-mail z linkiem aktywacyjnym asynchronicznie |
| `acceptInvitation(String token, String newPassword)` | Weryfikuje token zaproszenia, hashuje hasło BCrypt, zapisuje na użytkowniku, usuwa token (jednorazowy) |

---

### `LoginAttemptService`

| Metoda | Opis |
|---|---|
| `registerFailedAttempt(String email)` | Inkrementuje licznik nieudanych prób; po ≥ 3 próbach ustawia blokadę na 15 min w `ConcurrentHashMap` |
| `resetFailedAttempts(String email)` | Usuwa wpis z cache po udanym logowaniu |
| `isAccountLocked(String email)` | Zwraca `true` jeśli konto zablokowane; automatycznie czyści przeterminowaną blokadę |
| `getLockedUntil(String email)` | Zwraca `LocalDateTime` końca blokady lub `null` |

---

### `PasswordResetService`

| Metoda | Opis |
|---|---|
| `requestPasswordReset(String email)` | Generuje token UUID (TTL 1 h), wysyła e-mail asynchronicznie; **nie ujawnia** czy e-mail istnieje (silent fail) |
| `resetPassword(String token, String newPassword)` | Weryfikuje token, hashuje nowe hasło BCrypt, usuwa token |

---

### `RefreshTokenService`

| Metoda | Opis |
|---|---|
| `createRefreshToken(User user)` | Generuje nowy refresh token (UUID + expiry z JwtService), zapisuje w bazie |
| `exchange(String tokenValue)` | Weryfikuje token, unieważnia go (`revoked = true`), generuje nową parę (access + refresh); zwraca `TokenPair` |

---

## MODUŁ 2 — Serwisy: Zarządzanie Użytkownikami

### `AdminUserService`

| Metoda | Opis |
|---|---|
| `getAllUsers()` | Zwraca wszystkich nieusuniętych użytkowników jako `List<UserResponse>` (z przypisanym `apartmentId`) |
| `createUser(CreateUserRequest request)` | Tworzy konto z pustym hasłem, przypisuje do lokalu, wywołuje `InvitationService.inviteUser()` |
| `updateUser(UUID id, UpdateUserRequest request)` | Aktualizuje imię/nazwisko/telefon/rolę; jeśli `apartmentId != null` — zastępuje stare przypisanie do lokalu |
| `deactivateUser(UUID id)` | Ustawia `is_active = false`; dane historyczne zachowane |
| `deleteUser(UUID id)` | Soft delete przez `userRepository.delete()` (mapowane przez JPA jako soft-delete) |

---

### `UserService`

| Metoda | Opis |
|---|---|
| `getUsersWithActiveTicketCountByRole(String role)` | Zwraca użytkowników danej roli z liczbą aktywnych przypisanych zgłoszeń (`List<UserWithTicketsDto>`) — używane do wyboru konserwatora |

---

## MODUŁ 3 — Serwisy: Nieruchomości i Struktura Budynku

### `PropertyService`

| Metoda | Opis |
|---|---|
| `create(PropertyRequest request)` | Tworzy nieruchomość; waliduje unikalność NIP |
| `update(UUID id, PropertyRequest request)` | Aktualizuje dane; waliduje unikalność NIP z wykluczeniem bieżącego rekordu |
| `getAll()` | Zwraca `List<PropertyResponse>` |
| `getById(UUID id)` | Zwraca `PropertyResponse` lub rzuca `NotFoundException` |
| `uploadLogo(UUID id, MultipartFile file)` | Waliduje MIME (PNG/JPEG) i rozmiar (max 2 MB); zapisuje do `uploads/logos/{id}.{ext}`; aktualizuje `logo_path` |

---

### `BuildingService`

| Metoda | Opis |
|---|---|
| `getBuildingTree()` | Zwraca pełne drzewo budynki → klatki → lokale jako `List<BuildingTreeDto>` |
| `createBuilding(BuildingRequest request)` | Tworzy budynek, opcjonalnie przypisuje do nieruchomości |
| `updateBuilding(UUID id, BuildingRequest request)` | Aktualizuje budynek |
| `deleteBuilding(UUID id)` | Usuwa budynek; blokuje jeśli ma lokale (`BusinessValidationException`) |
| `createStaircase(UUID buildingId, StaircaseRequest request)` | Tworzy klatkę schodową w budynku |
| `updateStaircase(UUID buildingId, UUID staircaseId, StaircaseRequest request)` | Aktualizuje klatkę; weryfikuje przynależność do budynku |
| `deleteStaircase(UUID buildingId, UUID staircaseId)` | Usuwa klatkę; blokuje jeśli ma lokale |
| `createApartment(UUID staircaseId, ApartmentRequest request)` | Tworzy lokal w klatce |
| `updateApartment(UUID staircaseId, UUID apartmentId, ApartmentRequest request)` | Aktualizuje lokal; weryfikuje przynależność do klatki |
| `deleteApartment(UUID staircaseId, UUID apartmentId)` | Usuwa lokal; historyczne zgłoszenia nieruszone |

---

## MODUŁ 4 — Serwisy: Zgłoszenia Serwisowe

### `TicketService` (827 linii)

| Metoda | Opis |
|---|---|
| `initTicketNumberGenerator()` | `@EventListener(ApplicationReadyEvent)` — inicjalizuje generator numerów na podstawie max sekwencji z bazy |
| `create(TicketRequest request, String username)` | Tworzy zgłoszenie (status: `NOWE`); wymaga lokalu u autora; generuje unikalny numer |
| `getAll(String username, TicketFilterParams filters)` | Lista zgłoszeń wg roli: ZARZADCA (wszystkie + pełne filtry), KONSERWATOR (własne), MIESZKANIEC (lokal/klatka/budynek) |
| `getById(UUID ticketId, String username)` | Szczegóły; MIESZKANIEC nie widzi `internalNote`; KONSERWATOR tylko własne |
| `assignTicket(UUID ticketId, TicketAssignRequest request, String username)` | Status → `ZAPLANOWANO`; weryfikuje rolę KONSERWATOR przypisywanego; PUSH do autora |
| `closeTicket(UUID ticketId, String username)` | Status `ZAKONCZONE_DO_WERYFIKACJI` → `ZAMKNIETE`; generuje PDF protokołu; tworzy `Document`; PUSH ×2 |
| `rejectTicket(UUID ticketId, TicketRejectRequest request, String username)` | Status → `ODRZUCONE`; powód w `internalNote` i `TicketHistory`; PUSH do autora |
| `startWork(UUID ticketId, String username)` | Status `ZAPLANOWANO` → `W_REALIZACJI`; tylko przypisany KONSERWATOR; PUSH do autora |
| `suspendWork(UUID ticketId, TicketSuspendRequest request, String username)` | Status `W_REALIZACJI` → `WSTRZYMANO`; powód w `internalNote`; PUSH do wszystkich ZARZADCA |
| `completeWork(UUID ticketId, TicketCompletionRequest request, String username)` | Status `W_REALIZACJI` → `ZAKONCZONE_DO_WERYFIKACJI`; zapis `workDescription`; PUSH do zarządców |
| `changeStatus(UUID ticketId, TicketStatusChangeRequest request, String username)` | Generyczna zmiana statusu przez `TicketStateMachine.validateTransition()`; KONSERWATOR tylko własne |

---

### `TicketCommentService`

| Metoda | Opis |
|---|---|
| `addComment(UUID ticketId, TicketCommentRequest request, String email)` | Dodaje komentarz; walidacja per rola: MIESZKANIEC → tylko `PUBLICZNY`; KONSERWATOR → tylko `WEWNETRZNY` (własne zgłoszenia); ZARZADCA → oba |
| `getComments(UUID ticketId, String email)` | Lista komentarzy; MIESZKANIEC widzi tylko `PUBLICZNY` |

---

### `TicketCategoryService`

| Metoda | Opis |
|---|---|
| `getActiveCategories()` | Zwraca aktywne kategorie (`is_active = true`) jako `List<CategoryResponse>` (z `slaHours`) |
| `createCategory(CategoryRequest request)` | Tworzy kategorię (domyślnie aktywna) |
| `updateCategory(UUID id, CategoryRequest request)` | Zmiana nazwy kategorii |
| `setSlaHours(UUID id, SlaRequest request)` | Ustawia czas SLA (godziny robocze) dla kategorii |
| `deactivateCategory(UUID id)` | Soft-delete (`is_active = false`) |

---

## MODUŁ 5 — Serwisy: Ogłoszenia

### `AnnouncementService`

| Metoda | Opis |
|---|---|
| `getAnnouncementsForUser(String username)` | Ogłoszenia dopasowane do lokalu/klatki/budynku użytkownika; tylko z ostatnich 12 mies. |
| `createAnnouncement(AnnouncementRequest request, MultipartFile attachment, String username)` | Tworzy ogłoszenie; waliduje rolę ZARZADCA; opcjonalny załącznik PDF (max tylko PDF); asynchronicznie wywołuje PUSH FCM |
| `updateAnnouncement(UUID announcementId, AnnouncementRequest request, MultipartFile attachment, String username)` | Aktualizuje ogłoszenie; nowy załącznik zastępuje stary |
| `deleteAnnouncement(UUID announcementId, String username)` | Usuwa ogłoszenie |
| `sendPushNotificationsAsync(Announcement announcement)` | `@Async` — rozwiązuje lista adresatów wg `targetType`, wysyła FCM przez `PushNotificationService` |

---

## MODUŁ 6 — Serwisy: Uchwały i Głosowania

### `ResolutionService`

| Metoda | Opis |
|---|---|
| `castVote(UUID resolutionId, CastVoteRequest request, String username)` | Rejestruje głos; zabezpieczenie przed podwójnym głosem (unique constraint → HTTP 409) |
| `createResolution(CreateResolutionRequest request, String username)` | Tworzy uchwałę z listą opcji; weryfikuje rolę ZARZADCA w serwisie |
| `getResolutionsForUser(String username)` | ZARZADCA — wszystkie; MIESZKANIEC — powiązane z jego budynkiem |
| `getResolutionDetails(UUID resolutionId, String username)` | Szczegóły + opcje; wyniki (`results`) widoczne gdy: ZARZADCA LUB `endDate` minął LUB user już głosował |
| `generateResolutionReport(UUID resolutionId, String username)` | Generuje PDF raportu z głosowania (tabela opcji + liczby); dostępne tylko po `endDate` |

---

## MODUŁ 7 — Serwisy: Liczniki i Odczyty

### `MeterService`

| Metoda | Opis |
|---|---|
| `create(UUID apartmentId, MeterRequest request)` | Dodaje licznik do lokalu; waliduje unikalność `serialNumber` globalnie |
| `getAllByApartment(UUID apartmentId)` | Lista liczników lokalu (aktywne i nieaktywne) |
| `deactivate(UUID meterId)` | Dezaktywuje licznik (`is_active = false`); blokuje jeśli już nieaktywny |

---

### `MeterReadingService`

| Metoda | Opis |
|---|---|
| `create(UUID apartmentId, MeterReadingRequest request, String username)` | Dodaje odczyt; waliduje: rola KONSERWATOR (przypisane zgłoszenie), licznik aktywny + przypisany do lokalu, brak duplikatu, brak regresji wartości |
| `getAllByApartment(UUID apartmentId, int page, int size, String username)` | Stronicowana lista odczytów; MIESZKANIEC → tylko własny lokal |
| `getById(UUID id, String username)` | Szczegóły odczytu; row-level security: MIESZKANIEC (własny lokal), KONSERWATOR (przypisane zgłoszenie) |
| `update(UUID id, MeterReadingRequest request)` | Aktualizuje odczyt; rewalidacja duplikatów i regresji z pominięciem edytowanego rekordu |
| `delete(UUID id)` | Soft-delete (`deleted = true`) |

---

## MODUŁ 8 — Serwisy: Finanse i Transakcje

### `FinancialTransactionService`

| Metoda | Opis |
|---|---|
| `getTransactionsForApartment(UUID apartmentId)` | Historia transakcji + aktualne saldo z pola `current_balance` (`ApartmentTransactionsResponse`) |
| `createTransaction(UUID apartmentId, FinancialTransactionRequest request, String userEmail)` | `@Transactional` — tworzy transakcję i atomowo aktualizuje `current_balance` lokalu |
| `importTransactionsFromCsv(MultipartFile file, String userEmail)` | Import CSV (nagłówek: `apartment_id,date,type,amount,description`); per-wiersz walidacja UUID/daty/typu/kwoty; błędy zbierane w `CsvImportResultDto` bez przerywania |

---

### `ApartmentBalanceService`

| Metoda | Opis |
|---|---|
| `getBalances(UUID propertyId, BigDecimal minDebt, Long minDaysOverdue, boolean sortDesc)` | Zestawienie sald lokali z filtrowaniem (nieruchomość, min. dług, min. dni zalegania) i sortowaniem; oblicza `daysOverdue` od ostatniej wpłaty |

---

## MODUŁ 9 — Serwisy: Dokumenty

### `DocumentService`

| Metoda | Opis |
|---|---|
| `storeGeneratedDocument(String type, String title, byte[] pdfBytes, User ownerUser, Apartment apartment, Ticket ticket, Resolution resolution)` | Zapisuje PDF w `DocumentStorage`, tworzy wpis w tabeli `documents`; powiązania z lokal/zgłoszenie/uchwała opcjonalne |
| `getDocuments(UUID apartmentId, LocalDate startDate, LocalDate endDate, String type, String username)` | Lista z kontrolą dostępu: ZARZADCA (wszystkie + filtry DB), MIESZKANIEC (własne konto lub własny lokal) |
| `downloadDocument(UUID documentId, String username)` | Weryfikuje uprawnienia, zwraca `Resource` z `DocumentStorage` |

---

### `DocumentDistributionService`

| Metoda | Opis |
|---|---|
| `distributeRateChange(RateChangeDistributionRequest request, String managerEmail)` | `@Transactional` — generuje PDF zawiadomienia o stawkach, tworzy `Document` per mieszkaniec w zakresie, wysyła PUSH; zwraca `DocumentDistributionResult` |
| `distributeAnnualSettlement(AnnualSettlementDistributionRequest request, String managerEmail)` | `@Transactional` — dla każdego lokalu w zakresie: oblicza saldo otwarcia/zamknięcia za rok, generuje PDF rozliczenia, tworzy `Document` per mieszkaniec, wysyła PUSH |

---

## MODUŁ 10 — Serwisy: PDF

### `PdfGeneratorService`

| Metoda | Opis |
|---|---|
| `generateWorkAcceptanceProtocol(WorkAcceptanceProtocolRequest request)` | Deleguje do `WorkAcceptanceProtocolTemplate`; dane: numer zgłoszenia, opis prac, konserwator, ścieżki BEFORE/AFTER |
| `generateRateChangeNotification(String subject, String body, String effectiveDate, String communityName)` | Deleguje do `RateChangeNotificationTemplate` |
| `generateAnnualSettlement(AnnualSettlementData data)` | Deleguje do `AnnualSettlementTemplate` (dane: adres, rok, saldo otwarcia/zamknięcia, lista transakcji) |
| `generateBalancesReport(List<ApartmentBalanceResponse> rows)` | Deleguje do `BalancesReportTemplate`; mapuje DTO → `BalanceRow` |

---

## MODUŁ 11 — Serwisy: Przeglądy Techniczne

### `InspectionService`

| Metoda | Opis |
|---|---|
| `create(InspectionRequest request, String username)` | Tworzy przegląd; waliduje istnienie encji zasięgu (`scopeType` + `scopeId`) |
| `getAll(String username)` | ZARZADCA — wszystkie (sortowanie rosnąco po `scheduledAt`); pozostałe role — filtrowanie wg hierarchii lokalu (klatka → budynek → nieruchomość) |
| `update(UUID id, InspectionRequest request)` | Aktualizuje przegląd; rewalidacja zasięgu |
| `delete(UUID id)` | Hard delete przeglądu |

---

## MODUŁ 12 — Serwisy: Powiadomienia Push i Urządzenia

### `PushNotificationService`

| Metoda | Opis |
|---|---|
| `sendToUsers(List<UUID> userIds, String eventType, String title, String body, Map<String,String> data)` | `@Async` — wysyła FCM do listy użytkowników; najpierw sprawdza config globalny (`notification_config`), potem per-user (`notification_settings`) |
| `send(UUID userId, String eventType, String title, String body, Map<String,String> data)` | `@Async` — wysyła FCM do jednego użytkownika; ta sama hierarchia sprawdzeń |

*Metody prywatne: `sendToUser()`, `sendMessage()`, `isGloballyEnabled()`, `isEnabled()` — automatyczne usuwanie tokenów `UNREGISTERED` przy błędzie FCM*

---

### `UserDeviceService`

| Metoda | Opis |
|---|---|
| `registerDevice(UUID userId, String fcmToken, String platform)` | `@Transactional` — idempotentna rejestracja tokenu FCM; jeśli token przypisany do innego usera → stary wpis usuwany |
| `unregisterDevice(UUID userId, String fcmToken)` | `@Transactional` — usuwa token FCM |

---

### `NotificationConfigService`

| Metoda | Opis |
|---|---|
| `getAll()` | Lista wszystkich globalnych konfiguracji (`List<NotificationConfigResponse>` z etykietami PL) |
| `update(String eventType, boolean enabled)` | Włącza/wyłącza typ zdarzenia globalnie; zwraca zaktualizowane DTO |

---

## Weryfikacja pokrycia serwisów

| Serwis | Moduł | Status |
|---|---|---|
| `InvitationService` | 1 | ✅ |
| `LoginAttemptService` | 1 | ✅ |
| `PasswordResetService` | 1 | ✅ |
| `RefreshTokenService` | 1 | ✅ |
| `AdminUserService` | 2 | ✅ |
| `UserService` | 2 | ✅ |
| `PropertyService` | 3 | ✅ |
| `BuildingService` | 3 | ✅ |
| `TicketService` | 4 | ✅ |
| `TicketCommentService` | 4 | ✅ |
| `TicketCategoryService` | 4 | ✅ |
| `AnnouncementService` | 5 | ✅ |
| `ResolutionService` | 6 | ✅ |
| `MeterService` | 7 | ✅ |
| `MeterReadingService` | 7 | ✅ |
| `FinancialTransactionService` | 8 | ✅ |
| `ApartmentBalanceService` | 8 | ✅ |
| `DocumentService` | 9 | ✅ |
| `DocumentDistributionService` | 9 | ✅ |
| `PdfGeneratorService` | 10 | ✅ |
| `InspectionService` | 11 | ✅ |
| `PushNotificationService` | 12 | ✅ |
| `UserDeviceService` | 12 | ✅ |
| `NotificationConfigService` | 12 | ✅ |
| `TicketImageService` | 4 | ✅ (użyty w TicketImageController) |
| `TicketStateMachine` | 4 | ✅ (wywoływany przez TicketService) |
| `TicketNumberGenerator` | 4 | ✅ (wywoływany przez TicketService) |
| `BusinessHoursCalculator` | 4 | ✅ (wywoływany przez TicketService — SLA) |

**Pokrycie serwisów: 28/28 — 100%** ✅
