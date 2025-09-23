### GET /scenarios/{scenario_id}

* response 

```json
{
    "id" : "uuid",
    "name" : "string",
    "created_at" : "timestamp",
    "status" : "active | inactive",
    "lua_script" : "string",
    "owner_id" : "uuid",
    "household_id" : "uuid"
}
```


### POST /scenarios

* request body

```json
{
    "name" : "string",
    "created_at" : "timestamp",
    "status" : "active | inactive",
    "lua_script" : "string",
    "owner_id" : "uuid",
    "household_id" : "uuid"
}
```


### PUT /scenarios/{scenario_id}

* request body

```json
{
    "name" : "string",
    "created_at" : "timestamp",
    "status" : "active | inactive",
    "lua_script" : "string",
    "owner_id" : "uuid",
    "household_id" : "uuid"
}
```

### DELETE /scenarios/{scenario_id}

* response just http code