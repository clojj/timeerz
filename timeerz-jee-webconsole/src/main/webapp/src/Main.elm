module Main exposing (..)

import Debug

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
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
  , rsError : Maybe Http.Error
  , error : String
  , data : String
  }

type Msg
  = InputTimerId String
  | Send
  | NewMessage String
  | UpdateMessage String
  | SetTableState Table.State
  | LoadTimeerz (Result Http.Error String)


init : (Model, Cmd Msg)
init =
  (Model "" (Table.initialSort "Timer-ID") [] Nothing "" "waiting for update...", initialCmd)

timerInfoDecoder : Decode.Decoder TimerInfo
timerInfoDecoder =
    Decode.map2 TimerInfo
        (Decode.at [ "timerId" ] Decode.string)
        (Decode.at [ "timerData" ] Decode.string)

decodeTimerInfo : String -> Result String TimerInfo
decodeTimerInfo str =
    Decode.decodeString timerInfoDecoder str

timerInfoListDecoder : Decoder (List TimerInfo)
timerInfoListDecoder =
    Decode.list timerInfoDecoder

decodeTimerInfoList : String -> Result String (List TimerInfo)
decodeTimerInfoList str =
    Decode.decodeString timerInfoListDecoder str

encodeTimerId : TimerId -> Encode.Value
encodeTimerId { timerId } =
    Encode.object
        [ ("timerId", Encode.string timerId)
        ]

validateTimerId : Model -> String -> Bool
validateTimerId model id =
  (String.length id > 0) &&  List.member id (List.map .timerId model.timeerz)

initialCmd : Cmd Msg
initialCmd =
  Http.send LoadTimeerz (Http.getString "http://localhost:8080/timeerz-jee-demo-1.0-SNAPSHOT/rs/timeerz/list")

-- UPDATE


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =

  case msg of

    LoadTimeerz (Ok responseStr) ->
      case decodeTimerInfoList responseStr of
        Ok timeers -> ({model | timeerz = timeers } , Cmd.none)
        Err err    -> ({model | error = err}, Cmd.none)

    LoadTimeerz (Err err) ->
      ( {model | rsError = Just err}, Cmd.none )

    InputTimerId timerId ->
        if validateTimerId model timerId
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
        case decodeTimerInfo message of
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

httpError : Maybe Http.Error -> String
httpError err =
  case err of
    Nothing  -> ""
    Just err -> toString err

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
        [ div [ style [ ("color", "red") ], class "small" ]
          [ text (httpError model.rsError) ]
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
