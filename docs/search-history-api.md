# Search History API

Tai lieu nay mo ta API luu va lay lich su tim kiem cho FE.

## Base URL

```txt
http://localhost:8080
```

Neu BE chay port/domain khac, FE thay base URL tuong ung.

## Response Wrapper

Tat ca response thanh cong dang object JSON duoc boc theo format:

```json
{
  "statusCode": 200,
  "error": null,
  "message": "CALL API SUCCESS",
  "data": {}
}
```

Khi dung du lieu, FE lay payload chinh trong field `data`.

## Enum

Field `type` co 2 gia tri:

| Value | Y nghia |
| --- | --- |
| `QUERY` | User tim kiem bang tu khoa |
| `URL` | User click/truy cap vao mot ket qua/link |

## 1. Luu Search History

```http
POST /search/history
Content-Type: application/json
```

### Luu history loai QUERY

Dung khi user submit tu khoa tim kiem.

Request:

```json
{
  "type": "QUERY",
  "query": "benh cam cum"
}
```

Response:

```json
{
  "statusCode": 200,
  "error": null,
  "message": "CALL API SUCCESS",
  "data": {
    "id": 1,
    "type": "QUERY",
    "visitedAt": "2026-06-13T09:24:24.918",
    "query": "benh cam cum",
    "title": null,
    "url": null
  }
}
```

### Luu history loai URL

Dung khi user click/truy cap vao mot link ket qua.

Request:

```json
{
  "type": "URL",
  "title": "Bai viet y te",
  "url": "https://example.com/medical"
}
```

Response:

```json
{
  "statusCode": 200,
  "error": null,
  "message": "CALL API SUCCESS",
  "data": {
    "id": 2,
    "type": "URL",
    "visitedAt": "2026-06-13T09:24:25.022",
    "query": null,
    "title": "Bai viet y te",
    "url": "https://example.com/medical"
  }
}
```

### Validation

| Truong hop | Bat buoc |
| --- | --- |
| `type = QUERY` | `query` khong duoc rong |
| `type = URL` | `title` va `url` khong duoc rong |
| Moi request | `type` bat buoc co |

Neu sai validation, BE tra HTTP `400`.

Vi du response loi:

```json
{
  "statusCode": 400,
  "error": "Illegal argument exception occurs...",
  "message": "query is required when type is QUERY",
  "data": null
}
```

## 2. Lay Search History

```http
GET /search/history?page=0&size=10
```

Query params:

| Param | Type | Default | Ghi chu |
| --- | --- | --- | --- |
| `page` | number | `0` | Bat dau tu 0 |
| `size` | number | `10` | So item moi page |

Response:

```json
{
  "statusCode": 200,
  "error": null,
  "message": "CALL API SUCCESS",
  "data": {
    "totalResults": 2,
    "totalPages": 1,
    "page": 0,
    "size": 10,
    "items": [
      {
        "id": 2,
        "type": "URL",
        "visitedAt": "2026-06-13T09:24:25.022",
        "query": null,
        "title": "Bai viet y te",
        "url": "https://example.com/medical"
      },
      {
        "id": 1,
        "type": "QUERY",
        "visitedAt": "2026-06-13T09:24:24.918",
        "query": "benh cam cum",
        "title": null,
        "url": null
      }
    ]
  }
}
```

Ket qua duoc sap xep moi nhat truoc theo `visitedAt DESC`.

## FE Mapping Goi Y

FE co the map moi item ve shape dang dung:

```ts
type SearchHistoryType = "QUERY" | "URL";

type SearchHistoryItem = {
  id: number;
  type: SearchHistoryType;
  visitedAt: string;
  query: string | null;
  title: string | null;
  url: string | null;
};
```

Hien thi:

```ts
const label =
  item.type === "QUERY"
    ? item.query
    : item.title;
```

Khi click item:

```ts
if (item.type === "QUERY") {
  // fill search box bang item.query va goi API /search
}

if (item.type === "URL") {
  // open item.url
}
```

## Fetch Examples

### Save QUERY

```ts
await fetch(`${BASE_URL}/search/history`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    type: "QUERY",
    query,
  }),
});
```

### Save URL

```ts
await fetch(`${BASE_URL}/search/history`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    type: "URL",
    title,
    url,
  }),
});
```

### Get History

```ts
const res = await fetch(`${BASE_URL}/search/history?page=0&size=10`);
const body = await res.json();
const historyItems = body.data.items;
```
