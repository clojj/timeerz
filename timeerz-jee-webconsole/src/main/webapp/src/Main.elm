module Main exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import WebSocket
import Table
import Json.Decode as Decode exposing(..)
import Json.Encode as Encode exposing(..)
import Basics
import Regex

import Bootstrap.Grid as Grid
import Bootstrap.Grid.Col as Col
import Bootstrap.Grid.Row as Row

main =
  Html.program
    { init = init
    , view = view
    , update = update
    , subscriptions = subscriptions
    }


-- MODEL

type alias TimerInfo =
  { timerId : String
  , timerData : String
  }

type alias TimerId =
  { timerId : String
  }

type alias Model =
  { timerId : String
  , tableState : Table.State
  , timeerz : List TimerInfo
  , error : String
  , data : String
  }

type Msg
  = InputTimerId String
  | Send
  | NewMessage String
  | UpdateMessage String
  | SetTableState Table.State


init : (Model, Cmd Msg)
init =
  (Model "" (Table.initialSort "Timer-ID") [] "" "waiting for update...", Cmd.none)

timeerzDecoder : Decode.Decoder TimerInfo
timeerzDecoder =
    Decode.map2 TimerInfo
        (Decode.at [ "timerId" ] Decode.string)
        (Decode.at [ "timerData" ] Decode.string)

decodeTimeerz : String -> Result String TimerInfo
decodeTimeerz str =
    Decode.decodeString timeerzDecoder str

encodeTimerId : TimerId -> Encode.Value
encodeTimerId { timerId } =
    Encode.object
        [ ("timerId", Encode.string timerId)
        ]

-- UPDATE

validateTimerId : String -> Bool
validateTimerId =
  ((<) 0) << String.length

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =

  case msg of

    InputTimerId timerId ->
        if validateTimerId timerId
        then ({model | timerId = timerId, error = ""}, Cmd.none)
        else
          let errMsg = case timerId of
                          "" -> ""
                          _  -> "invalid ID"
          in ({model | timerId = timerId, error = errMsg}, Cmd.none)

    Send ->
        let res = encodeTimerId (TimerId model.timerId)
        in ({model | timerId = ""}, WebSocket.send "ws://localhost:8080/timeerz-jee-demo-1.0-SNAPSHOT/timeerz" (Encode.encode 1 res))

    UpdateMessage data ->
        ({model | data = data}, Cmd.none)

    NewMessage message ->
        case decodeTimeerz message of
            Ok timeer -> ({model | timeerz = timeer :: model.timeerz} , Cmd.none)
            Err err        -> ({model | error = err}, Cmd.none)

    SetTableState newState ->
      ( {model | tableState = newState}, Cmd.none )


-- SUBSCRIPTIONS

subscriptions : Model -> Sub Msg
subscriptions model =
  Sub.batch
    [
      WebSocket.listen "ws://localhost:8080/timeerz-jee-demo-1.0-SNAPSHOT/timeerz" NewMessage
    , WebSocket.listen "ws://localhost:8080/timeerz-jee-demo-1.0-SNAPSHOT/update" UpdateMessage
    ]


-- VIEW

invalidInput : Model -> Bool
invalidInput { timerId, error } =
  error /= "" || timerId == ""


view : Model -> Html Msg
view model =
  Grid.container []
    [ Grid.row []
      [ Grid.col [ Col.xs12, Col.mdAuto ]
        [ input [placeholder "Timer-ID", onInput InputTimerId, Html.Attributes.value model.timerId] []
        , button [onClick Send, disabled (invalidInput model)] [text "Disable"]
        ]
      ]
    , Grid.row []
      [ Grid.col [ Col.xs12, Col.mdAuto ]
        [ div [ style [ ("color", "red") ], class "small" ]
          [ text model.error ]
        ]
      ]
    , Grid.row []
      [ Grid.col [ Col.xs12, Col.mdAuto ]
        [ div []
          [ label [] [ text "All active timeerz" ]
          , Table.view config model.tableState model.timeerz ]
          ]
      ]
    , Grid.row []
      [ Grid.col [ Col.xs12, Col.mdAuto ]
        [ div []
          [ text model.data ]
          ]
      ]
    ]

config : Table.Config TimerInfo Msg
config =
  Table.config
    { toId = .timerId
    , toMsg = SetTableState
    , columns =
        [ Table.stringColumn "Timer-ID" .timerId
        , Table.stringColumn "Data" .timerData
        ]
    }
