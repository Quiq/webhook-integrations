# Centricient Public API

The Centricient Platform allows you to perform various tasks through a REST API. These tasks range from checking if
there are agents available to downloading assets sent to and from agents within a conversation.

## Base URL

All URLs referenced in the documentation will have the following base:

`https://{company}.centricient.com/api/v0`

Replace `{company}` with the subdomain used to login to your Centricient site. E.g. if you use
`https://greatcompany.centricient.com/` to login, your base url would be `https://greatcompany.centricient.com/api/v0`.

The Centricient REST API is served over HTTPS. To ensure data privacy, unencrypted HTTP is not supported.

## Authentication

HTTP requests to the REST API are protected with [HTTP Basic
authentication](https://en.wikipedia.org/wiki/Basic_access_authentication). In short, you will register a client
application with us which will give you a `ClientId` and `ClientSecret` that will be used as the username and password
for the HTTP Basic authentication.

## Client App Registration

We are still building out our admin dashboard. In the meantime, we've put together a basic client app administration
page available at `https://{company}.centricient.com/external/hooks/development`, where `{company}` is the subdomain
used to login to your Centricient site. You must remain logged in to the primary messaging app while using this
administration page.

# Resources

## Test

`GET /whoami`

Returns information about your client app. This can be used to verify that your credentials are setup correctly.

Response:
```
{
  "id": "string",
  "name": "string",
  "tenant": "string"
}
```

| Property | Description|
|---|---|
|id|Your client app id.|
|name|The name of your client app.|
|tenant|The name that uniquely identifies your company within the Centricient system.|

**Example**

`curl --user "YOUR_CLIENT_ID:YOUR_CLIENT_SECRET" 'https://{company}.centricient.com/api/v0/whoami'`

## Assets

`GET /assets/{assetId}`

Fetches the asset.

| Path Parameter | Description|
|---|---|
|assetId|The asset id to fetch.|

**Example**

`curl --user "YOUR_CLIENT_ID:YOUR_CLIENT_SECRET" 'https://{company}.centricient.com/api/v0/assets/6a357fe36e315548cf6c3e5' > picture.png`

## Agents

`GET /agents-available`

Checks if there are any agents set to the available status.

Response:
```
{
    "available": "boolean"
}
```

| Property | Description|
|---|---|
|available|True if there are agents available, false otherwise.|

**Example**

`curl --user "YOUR_CLIENT_ID:YOUR_CLIENT_SECRET" 'https://{company}.centricient.com/api/v0/agents-available`

