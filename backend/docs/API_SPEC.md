# API Spec

## Health Check

- Method: `GET`
- Path: `/api/health`
- Auth: Public
- Description: Checks whether the backend application is running.

Response:

```json
{
  "code": 200,
  "message": "success",
  "data": "LifeLink backend is running"
}
```

## Register

- Method: `POST`
- Path: `/api/auth/register`
- Auth: Public
- Description: Creates a user account.

Request:

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "phone": "13800000000",
  "password": "123456"
}
```

Rules:

- `username` is required, length 3-50.
- `password` is required, length at least 6.
- At least one of `email` or `phone` is required.
- `username`, `email`, and `phone` must be unique.

Response:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com",
    "phone": "13800000000",
    "avatarUrl": null,
    "status": "ACTIVE",
    "createdAt": "2026-05-08T16:00:00"
  }
}
```

## Login

- Method: `POST`
- Path: `/api/auth/login`
- Auth: Public
- Description: Logs in with username, email, or phone.

Request:

```json
{
  "account": "alice",
  "password": "123456"
}
```

Response:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "user": {
      "id": 1,
      "username": "alice",
      "email": "alice@example.com",
      "phone": "13800000000",
      "avatarUrl": null,
      "status": "ACTIVE",
      "createdAt": "2026-05-08T16:00:00"
    }
  }
}
```

## Current User

- Method: `GET`
- Path: `/api/user/me`
- Auth: Required
- Header: `Authorization: Bearer <token>`
- Description: Returns current logged-in user profile.

Response:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com",
    "phone": "13800000000",
    "avatarUrl": null,
    "status": "ACTIVE",
    "createdAt": "2026-05-08T16:00:00"
  }
}
```

## Create Relationship

- Method: `POST`
- Path: `/api/relationships`
- Auth: Required

Request:

```json
{
  "name": "Our Home",
  "type": "FAMILY",
  "description": "Shared family space"
}
```

Rules:

- `name` is required, length 1-100.
- `type` must be one of `COUPLE`, `FAMILY`, `FRIEND`, `ROOMMATE`, `CUSTOM`.
- Creator becomes `OWNER`.

## List Relationships

- Method: `GET`
- Path: `/api/relationships`
- Auth: Required
- Description: Lists active relationship spaces where current user is a member.

## Get Relationship Detail

- Method: `GET`
- Path: `/api/relationships/{id}`
- Auth: Required
- Description: Returns relationship info and current user's role. Current user must be a member.

## List Relationship Members

- Method: `GET`
- Path: `/api/relationships/{id}/members`
- Auth: Required
- Description: Returns active members in a relationship. Current user must be an active member.

Response item:

```json
{
  "userId": 1,
  "username": "alice",
  "avatarUrl": null,
  "role": "OWNER",
  "nickname": null,
  "joinedAt": "2026-05-08T16:00:00"
}
```

## Update My Relationship Nickname

- Method: `PATCH`
- Path: `/api/relationships/{id}/members/me/nickname`
- Auth: Required
- Description: Updates current user's nickname inside the relationship space.

Request:

```json
{
  "nickname": "Alice"
}
```

Rules:

- Current user must be an active member.
- `nickname` is optional and must be at most 50 characters.

## Leave Relationship

- Method: `POST`
- Path: `/api/relationships/{id}/leave`
- Auth: Required
- Description: Current user leaves the relationship space by setting member status to `LEFT`.

Rules:

- Current user must be an active member.
- `OWNER` cannot leave while other active members exist; owner must transfer ownership or dissolve the space first.
- If the owner is the only member, leaving also soft-deletes the relationship.

## Dissolve Relationship

- Method: `DELETE`
- Path: `/api/relationships/{id}`
- Auth: Required
- Description: Soft deletes the relationship by setting `relationships.status` to `DELETED`.

Rules:

- Only `OWNER` can dissolve the space.
- Active members are marked `REMOVED`.

## Update Member Role

- Method: `PATCH`
- Path: `/api/relationships/{id}/members/{userId}/role`
- Auth: Required

Request:

```json
{
  "role": "ADMIN"
}
```

Rules:

- Only `OWNER` can update roles.
- `role` can be `ADMIN` or `MEMBER`.
- Owner role transfer must use the transfer-owner endpoint.

## Remove Relationship Member

- Method: `DELETE`
- Path: `/api/relationships/{id}/members/{userId}`
- Auth: Required
- Description: Marks the target member as `REMOVED`.

Rules:

- Only `OWNER` can remove members.
- `OWNER` cannot be removed.

## Transfer Relationship Owner

- Method: `POST`
- Path: `/api/relationships/{id}/transfer-owner`
- Auth: Required

Request:

```json
{
  "targetUserId": 2
}
```

Rules:

- Only current `OWNER` can transfer ownership.
- Target user must be an active member.
- Previous owner becomes `ADMIN`.

## Create Relationship Invite

- Method: `POST`
- Path: `/api/relationships/{id}/invite`
- Auth: Required
- Description: Creates an invite code. Current user must be `OWNER` or `ADMIN`.

Response:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "inviteCode": "ABCD2345",
    "expireAt": "2026-05-15T16:00:00"
  }
}
```

## Join Relationship

- Method: `POST`
- Path: `/api/relationships/join`
- Auth: Required

Request:

```json
{
  "inviteCode": "ABCD2345"
}
```

Rules:

- Invite code must exist and be `ACTIVE`.
- Invite code must not be expired.
- Current user must not already be a member.
- Joined user gets `MEMBER` role.

## Create Daily Post

- Method: `POST`
- Path: `/api/daily-posts`
- Auth: Required
- Description: Creates a daily post in a relationship space, supports text and images.

Request:

```json
{
  "relationshipId": 1,
  "content": "Today was a good day.",
  "mood": "HAPPY",
  "visibility": "RELATIONSHIP",
  "imageIds": [101, 102]
}
```

Rules:

- Current user must be a member of the relationship.
- `content` is required.
- Creator is the current logged-in user.
- `imageIds` is optional, maximum 9.
- Images must be uploaded by current user.

## List Daily Posts

- Method: `GET`
- Path: `/api/daily-posts`
- Auth: Required

Query parameters:

- `relationshipId`: optional
- `page`: default `1`
- `size`: default `10`

Rules:

- If `relationshipId` is provided, current user must be a member of that relationship.
- If `relationshipId` is omitted, returns posts from all relationship spaces current user joined.
- Only `ACTIVE` posts are returned.
- Results are ordered by `created_at` descending.

## Get Daily Post Detail

- Method: `GET`
- Path: `/api/daily-posts/{id}`
- Auth: Required
- Description: Returns post detail with image list. Current user must be a member of the post's relationship.

## Delete Daily Post

- Method: `DELETE`
- Path: `/api/daily-posts/{id}`
- Auth: Required
- Description: Soft deletes a daily post by setting `status` to `DELETED`.

Rules:

- Current user must be a member of the relationship.
- Only the author can delete the post.

## Like Daily Post

- Method: `POST`
- Path: `/api/daily-posts/{postId}/like`
- Auth: Required
- Description: Likes a daily post. Calling repeatedly is idempotent.
- Returns: `dailyPostId`, `likeCount`, `commentCount`, `likedByMe`.

## Unlike Daily Post

- Method: `DELETE`
- Path: `/api/daily-posts/{postId}/like`
- Auth: Required
- Description: Removes current user's like. Calling when not liked is still successful.

## Comment Daily Post

- Method: `POST`
- Path: `/api/daily-posts/{postId}/comments`
- Auth: Required

Request:

```json
{
  "content": "Looks wonderful!"
}
```

Rules:

- Current user must be a member of the post relationship space.
- `content` length must be 1-1000.

## List Daily Post Comments

- Method: `GET`
- Path: `/api/daily-posts/{postId}/comments`
- Auth: Required
- Query: `page`, `size`.
- Description: Returns active comments ordered by `created_at` ascending.

## Delete Daily Post Comment

- Method: `DELETE`
- Path: `/api/daily-posts/{postId}/comments/{commentId}`
- Auth: Required
- Description: Soft deletes a comment. Only the comment author can delete it.

## Get Daily Post Interactions

- Method: `GET`
- Path: `/api/daily-posts/{postId}/interactions`
- Auth: Required
- Returns: `dailyPostId`, `likeCount`, `commentCount`, `likedByMe`.

## Upload File

- Method: `POST`
- Path: `/api/files/upload`
- Auth: Required
- Content-Type: `multipart/form-data`
- Form field: `file`

Rules:

- Only `jpg`, `jpeg`, `png`, `webp` are allowed.
- Max size is 5MB.
- Uploaded to MinIO bucket `lifelink`.
- File metadata is stored in `file_resources`.

Response:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "fileId": 101,
    "url": "http://localhost:9000/lifelink/daily/2026/05/uuid.jpg",
    "objectKey": "daily/2026/05/uuid.jpg",
    "originalName": "photo.jpg",
    "contentType": "image/jpeg",
    "fileSize": 123456
  }
}
```

## Create Space Todo

- Method: `POST`
- Path: `/api/relationships/{relationshipId}/todos`
- Auth: Required

Request:

```json
{
  "title": "Buy groceries",
  "content": "Milk and fruit",
  "priority": "NORMAL",
  "dueTime": "2026-05-09T18:00:00"
}
```

Rules:

- Current user must be a relationship member.
- `title` is required, length 1-100.
- `priority` can be `LOW`, `NORMAL`, or `HIGH`.

## List Space Todos

- Method: `GET`
- Path: `/api/relationships/{relationshipId}/todos`
- Auth: Required

Query parameters:

- `status`: optional, `TODO` or `DONE`
- `keyword`: optional
- `page`: default `1`
- `size`: default `20`

Rules:

- Current user must be a relationship member.
- `DELETED` todos are not returned.

## Get Space Todo Detail

- Method: `GET`
- Path: `/api/relationships/{relationshipId}/todos/{todoId}`
- Auth: Required
- Description: Returns todo detail. Todo must belong to this relationship.

## Update Space Todo

- Method: `PUT`
- Path: `/api/relationships/{relationshipId}/todos/{todoId}`
- Auth: Required
- Description: Any relationship member can update a non-deleted todo.

## Toggle Space Todo

- Method: `PATCH`
- Path: `/api/relationships/{relationshipId}/todos/{todoId}/toggle`
- Auth: Required
- Description: Toggles `TODO` to `DONE`, or `DONE` to `TODO`.

## Delete Space Todo

- Method: `DELETE`
- Path: `/api/relationships/{relationshipId}/todos/{todoId}`
- Auth: Required
- Description: Soft deletes a todo by setting `status` to `DELETED`.

## List Account Books

- Method: `GET`
- Path: `/api/account-books`
- Auth: Required
- Description: Returns current user's personal account books and relationship account books from joined spaces.

## Create Account Book

- Method: `POST`
- Path: `/api/account-books`
- Auth: Required

Request:

```json
{
  "name": "My Wallet",
  "type": "PERSONAL",
  "relationshipId": null
}
```

Rules:

- `type` can be `PERSONAL` or `RELATIONSHIP`.
- `RELATIONSHIP` books require current user to be a relationship member.

## List Transaction Categories

- Method: `GET`
- Path: `/api/transaction-categories`
- Auth: Required
- Query: `type` optional, `INCOME` or `EXPENSE`.

## Create Transaction

- Method: `POST`
- Path: `/api/transactions`
- Auth: Required

Request:

```json
{
  "accountBookId": 1,
  "type": "EXPENSE",
  "amount": 35.50,
  "categoryId": 1,
  "title": "Lunch",
  "note": "Noodles",
  "transactionTime": "2026-05-08T12:30:00"
}
```

Rules:

- Current user must have access to the account book.
- `amount` must be greater than 0.

## List Transactions

- Method: `GET`
- Path: `/api/transactions`
- Auth: Required
- Query: `accountBookId`, `relationshipId`, `type`, `startDate`, `endDate`, `page`, `size`.
- Description: Returns only transactions in account books current user can access.

## Update Transaction

- Method: `PUT`
- Path: `/api/transactions/{id}`
- Auth: Required
- Description: Updates a transaction. Space members can edit shared book transactions; personal transactions are limited to owner access.

## Delete Transaction

- Method: `DELETE`
- Path: `/api/transactions/{id}`
- Auth: Required
- Description: Soft deletes a transaction by setting `status` to `DELETED`.

## Monthly Finance Summary

- Method: `GET`
- Path: `/api/statistics/finance/monthly`
- Auth: Required
- Query: `accountBookId`, `relationshipId`, `month` in `yyyy-MM`.
- Returns: `totalIncome`, `totalExpense`, `balance`.

## Category Finance Statistic

- Method: `GET`
- Path: `/api/statistics/finance/category`
- Auth: Required
- Query: `accountBookId`, `relationshipId`, `type`, `month` in `yyyy-MM`.
- Returns category amount and percentage.

## Create Anniversary

- Method: `POST`
- Path: `/api/anniversaries`
- Auth: Required

Request:

```json
{
  "relationshipId": 1,
  "title": "Birthday",
  "description": "A special day",
  "anniversaryDate": "2026-06-01",
  "repeatType": "YEARLY",
  "backgroundFileId": 1
}
```

Rules:

- Current user must be a member of the relationship space.
- `repeatType` can be `NONE` or `YEARLY`, defaulting to `NONE`.
- `backgroundFileId` must be an image uploaded by the current user.

## List Anniversaries

- Method: `GET`
- Path: `/api/anniversaries`
- Auth: Required
- Query: `relationshipId`, `repeatType`, `displayType`, `keyword`, `page`, `size`.
- Description: Returns active anniversaries in relationship spaces visible to current user.
- `displayType` can be `COUNTDOWN`, `PASSED`, or `TODAY`.

## Get Anniversary Detail

- Method: `GET`
- Path: `/api/anniversaries/{id}`
- Auth: Required
- Description: Current user must be a member of the anniversary relationship space.

## Update Anniversary

- Method: `PUT`
- Path: `/api/anniversaries/{id}`
- Auth: Required
- Description: Space members can update title, description, date, repeat type, and background image.

## Delete Anniversary

- Method: `DELETE`
- Path: `/api/anniversaries/{id}`
- Auth: Required
- Description: Soft deletes an anniversary by setting `status` to `DELETED`.

## List Relationship Activities

- Method: `GET`
- Path: `/api/relationships/{relationshipId}/activities`
- Auth: Required
- Query: `activityType`, `page`, `size`.
- Description: Returns active activity records for one relationship space. Current user must be a space member.

## List My Activities

- Method: `GET`
- Path: `/api/activities`
- Auth: Required
- Query: `activityType`, `page`, `size`.
- Description: Returns active activity records from all relationship spaces current user belongs to.

Activity types:

- `RELATIONSHIP_CREATED`
- `MEMBER_JOINED`
- `MEMBER_LEFT`
- `MEMBER_REMOVED`
- `MEMBER_ROLE_UPDATED`
- `OWNER_TRANSFERRED`
- `RELATIONSHIP_DELETED`
- `DAILY_POST_CREATED`
- `DAILY_POST_COMMENTED`
- `DAILY_POST_UPDATED`
- `DAILY_POST_DELETED`
- `TODO_CREATED`
- `TODO_COMPLETED`
- `TODO_REOPENED`
- `TODO_UPDATED`
- `TODO_DELETED`
- `ANNIVERSARY_CREATED`
- `ANNIVERSARY_UPDATED`
- `ANNIVERSARY_DELETED`

Target types:

- `RELATIONSHIP`
- `USER`
- `DAILY_POST`
- `DAILY_POST_COMMENT`
- `SPACE_TODO`
- `ANNIVERSARY`

Automatic activity generation currently covers:

- relationship creation
- member join
- member leave
- member removal
- member role update
- owner transfer
- relationship dissolution
- daily post creation
- daily post comment creation
- todo creation
- todo completed
- todo reopened
- anniversary creation

## Global Search

- Method: `GET`
- Path: `/api/search`
- Auth: Required

Query parameters:

- `keyword`: required, length 1-100.
- `types`: optional comma-separated list, supports `RELATIONSHIP`, `DAILY_POST`, `TODO`, `ANNIVERSARY`, `ACTIVITY`.
- `page`: optional, reserved for future pagination.
- `size`: optional, per-group result limit, default `8`, max `20`.

Rules:

- Only searches data in active relationship spaces where current user is an active member.
- Deleted relationship spaces, left/removed memberships, deleted daily posts, deleted todos, deleted anniversaries, and deleted activities are excluded.
- First version uses PostgreSQL `ILIKE` fuzzy matching on existing business tables.

Response:

```json
{
  "keyword": "birthday",
  "totalCount": 2,
  "groups": [
    {
      "type": "ANNIVERSARY",
      "title": "Anniversaries",
      "count": 1,
      "items": [
        {
          "id": 1,
          "type": "ANNIVERSARY",
          "title": "Birthday",
          "description": "A special day",
          "relationshipId": 1,
          "relationshipName": "Our Home",
          "targetUrl": "/anniversaries/1",
          "createdAt": "2026-05-13T10:00:00",
          "metadata": {
            "repeatType": "YEARLY"
          }
        }
      ]
    }
  ]
}
```

## Relationship Timeline

Relationship timeline stores curated relationship milestones. It is different from `space_activities`: activities are operational logs, while timeline events are meaningful nodes for later review.

### List Timeline Events

- Method: `GET`
- Path: `/api/relationships/{relationshipId}/timeline`
- Auth: Required

Query parameters:

- `eventType`: optional.
- `importance`: optional, `NORMAL` or `IMPORTANT`.
- `order`: optional, `ASC` or `DESC`, default `ASC`.

Rules:

- Current user must be an active member of the relationship.
- Relationship must be active.
- Only `ACTIVE` timeline events are returned.

### Timeline Event Detail

- Method: `GET`
- Path: `/api/relationships/{relationshipId}/timeline/{eventId}`
- Auth: Required

Rules:

- Current user must be an active member.
- The event must belong to the path relationship.

### Create Custom Timeline Event

- Method: `POST`
- Path: `/api/relationships/{relationshipId}/timeline`
- Auth: Required

Request:

```json
{
  "title": "A special day",
  "description": "We moved into our first home",
  "eventDate": "2026-05-14T20:00:00",
  "coverFileId": 1,
  "importance": "IMPORTANT"
}
```

Rules:

- Current user must be an active relationship member.
- `coverFileId`, when present, must be an image uploaded by current user.
- Created events use `eventType = CUSTOM` and `source = MANUAL`.

### Delete Timeline Event

- Method: `DELETE`
- Path: `/api/relationships/{relationshipId}/timeline/{eventId}`
- Auth: Required

Rules:

- Current user must be an active relationship member.
- The event must belong to the path relationship.
- Delete is soft delete: `status = DELETED`.

Automatic milestone generation currently covers:

- relationship creation
- member join
- first daily post per relationship
- anniversary creation
- completed `HIGH` priority todo
- daily post comment count first reaching 5
- daily post with uploaded images

## Life Calendar

Life Calendar aggregates relationship-space todos, anniversaries, daily posts, transactions, holidays, solar terms, and custom calendar events. All relationship-scoped endpoints require the current user to be an active member of the relationship.

### Get Month Calendar

- Method: `GET`
- Path: `/api/calendar/month`
- Auth: Required
- Query: `relationshipId`, `year`, `month`
- Description: Returns one month of aggregated calendar days for the relationship.

### Get Day Calendar

- Method: `GET`
- Path: `/api/calendar/day`
- Auth: Required
- Query: `relationshipId`, `date` in `yyyy-MM-dd`.
- Description: Returns one day's aggregated calendar detail.

### Create Calendar Event

- Method: `POST`
- Path: `/api/calendar/events`
- Auth: Required
- Body: `relationshipId`, `title`, `description`, `eventType`, `startTime`, `endTime`, `allDay`, `repeatType`, `reminderMinutes`, `color`.
- Description: Creates a user-defined calendar event in a relationship space.

### Update Calendar Event

- Method: `PUT`
- Path: `/api/calendar/events/{eventId}`
- Auth: Required
- Description: Creator can update the event. Relationship `OWNER` and `ADMIN` can update any event in the space.

### Delete Calendar Event

- Method: `DELETE`
- Path: `/api/calendar/events/{eventId}`
- Auth: Required
- Description: Soft deletes the event by setting `calendar_events.status` to `DELETED`.
