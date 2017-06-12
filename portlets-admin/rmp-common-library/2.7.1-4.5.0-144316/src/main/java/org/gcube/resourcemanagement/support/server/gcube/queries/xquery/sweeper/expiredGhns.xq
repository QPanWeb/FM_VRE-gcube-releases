<!--
    These are the GHNs that are declared certified and must be passed to
    down status.
    The choice is done by minutes elapsed from last update.
-->
let $RIs := collection("/db/Profiles/RunningInstance")//Document/Data/child::*[local-name()='Profile']/Resource
for $GHNs in collection("/db/Profiles/GHN")//Document/Data/child::*[local-name()='Profile']/Resource
let $RIinstalled := $RIs[Profile/GHN/string(@UniqueID)=$GHNs/ID]
let $scopes := string-join( $GHNs/Scopes//Scope/text(), ';')
where $GHNs/Profile/GHNDescription/Status/string() != 'down' and $GHNs/Profile/GHNDescription/Status/string() != 'unreachable'
return
 <RESOURCE/>