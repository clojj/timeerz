module Model exposing (..)

import Http
import Table exposing (defaultCustomizations)


type alias TimerData =
    { timerId : String
    , active : Bool
    , cronExpression : String
    }


type alias Model =
    { timerId : String
    , tableState : Table.State
    , timeerz : List TimerData
    , toggled : List String
    , rsError : Maybe Http.Error
    , error : String
    , jobCompletions : List String
    }


type Msg
    = InputTimerId String
    | ToggleSelected String
    | Send
    | NewMessage String
    | UpdateMessage String
    | SetTableState Table.State
    | LoadTimeerz (Result Http.Error String)


initialCmd : Cmd Msg
initialCmd =
    Http.send LoadTimeerz (Http.getString "http://localhost:8080/rs/timeerz/list")


initModel : ( Model, Cmd Msg )
initModel =
    ( Model "" (Table.initialSort "Timer-ID") [] [] Nothing "" [], initialCmd )
