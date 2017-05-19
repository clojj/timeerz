module Main exposing (..)

import Debug

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import WebSocket
import Table exposing (defaultCustomizations)
import Json.Decode as Decode exposing(..)
import Json.Encode as Encode exposing(..)
import Basics
import Regex
import List as List
import Model exposing (..)

import Bootstrap.Grid as Grid
import Bootstrap.Grid.Col as Col
import Bootstrap.Grid.Row as Row

main =
  Html.program
    { init = initModel
    , view = view
    , update = update
    , subscriptions = subscriptions
    }


-- TYPES

type alias TimerCommands = List TimerCommand

type alias TimerCommand =
  { timerId : String
  , timerOp : TimerOp
  }

type TimerOp
  = ToggleActivation
  | Reconfigure String


-- Json En-/Decoders

timerDataDecoder : Decode.Decoder TimerData
timerDataDecoder =
    Decode.map3 TimerData
        (Decode.at [ "timerId" ] Decode.string)
        (Decode.at [ "active" ] Decode.bool)
        (Decode.at [ "cronExpression" ] Decode.string)

decodeTimerData : String -> Result String TimerData
decodeTimerData str =
    Decode.decodeString timerDataDecoder str

timerDataListDecoder : Decoder (List TimerData)
timerDataListDecoder =
    Decode.list timerDataDecoder

decodeTimerDataList : String -> Result String (List TimerData)
decodeTimerDataList str =
    Decode.decodeString timerDataListDecoder str

encodeTimerCommands : List TimerCommand -> Encode.Value
encodeTimerCommands =
  List.map encodeTimerCommand >> Encode.list

encodeTimerCommand : TimerCommand -> Encode.Value
encodeTimerCommand { timerId, timerOp } =
    Encode.object
        [ ("timerId", Encode.string timerId)
        , ("timerOp", Encode.string (toString timerOp))
        ]


validateTimerId : Model -> String -> Bool
validateTimerId model id =
  (String.length id > 0) &&  List.member id (List.map .timerId model.timeerz)


-- UPDATE


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =

  case msg of

    ToggleSelected id ->
      let newTimeerz = List.map (toggle id) model.timeerz
          newToggled = if (List.member id model.toggled) then model.toggled else (id :: model.toggled)
      in
        ( { model | timeerz = newTimeerz , toggled = newToggled } , Cmd.none )

    Send ->
        let commands = encodeTimerCommands (List.map createTimerCommandToggled model.toggled)
        in ({model | timerId = "", toggled = []}, WebSocket.send "ws://localhost:8080/timeerz-jee-demo-1.0-SNAPSHOT/timeerz" (Encode.encode 1 commands))

    LoadTimeerz (Ok responseStr) ->
      case decodeTimerDataList responseStr of
        Ok timeers -> ({model | timeerz = timeers } , Cmd.none)
        Err err    -> ({model | error = err}, Cmd.none)

    LoadTimeerz (Err err) ->
      ( {model | rsError = Just err}, Cmd.none )

    InputTimerId timerId ->
        if validateTimerId model timerId
        then
          ({model | timerId = timerId, error = ""}, Cmd.none)
        else
          let errMsg = case timerId of
                          "" -> ""
                          _  -> "invalid ID"
          in ({model | timerId = timerId, error = errMsg}, Cmd.none)

    UpdateMessage event -> ({model | jobCompletions = event :: model.jobCompletions}, Cmd.none)

    NewMessage message ->
        case decodeTimerData message of
            Ok timeer -> ({model | timeerz = timeer :: model.timeerz} , Cmd.none)
            Err err   -> ({model | error = err}, Cmd.none)

    SetTableState newState ->
      ( {model | tableState = newState}, Cmd.none )

createTimerCommandToggled : String -> TimerCommand
createTimerCommandToggled timerId =
  (TimerCommand timerId ToggleActivation)

toggle : String -> TimerData -> TimerData
toggle id timerData =
  if timerData.timerId == id then
    { timerData | active = not timerData.active }
  else
    timerData

toggleFilter : String -> TimerData -> Bool
toggleFilter id timerData =
  if timerData.timerId == id then
    True
  else
    False

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
        [ button [onClick Send] [text "Send"]
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
          [ br [] []
          , label [] [ text "Job completed from timer:" ]
          , br [] []
          , renderList model.jobCompletions ]
          ]
      ]
    ]

-- TODO animation: scroll + fade-in
renderList lst =
    lst
       |> List.map (\l -> li [] [ text l ])
       |> ul []

config : Table.Config TimerData Msg
config =
  Table.customConfig
    { toId = .timerId
    , toMsg = SetTableState
    , columns =
      [ Table.stringColumn "Timer-ID" .timerId
      , checkboxColumn
--      TODO cron column
      ]
    , customizations =
      { defaultCustomizations | rowAttrs = toRowAttrs }
    }

toRowAttrs : TimerData -> List (Attribute Msg)
toRowAttrs timerData =
  [ onClick (ToggleSelected timerData.timerId)
  , style [ ("background", if timerData.active then "#CEFAF8" else "white") ]
  ]

checkboxColumn : Table.Column TimerData Msg
checkboxColumn =
  Table.veryCustomColumn
    { name = "Active"
    , viewData = viewCheckbox
    , sorter = Table.unsortable
    }

viewCheckbox : TimerData -> Table.HtmlDetails Msg
viewCheckbox { active } =
  Table.HtmlDetails []
    [ input [ type_ "checkbox", checked active ] []
    ]
