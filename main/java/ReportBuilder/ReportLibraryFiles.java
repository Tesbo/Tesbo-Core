package ReportBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ReportLibraryFiles {


    String chrome = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgdmVyc2lvbj0iMS4xIiBpZD0iTGF5ZXJfMSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgeD0iMHB4IiB5PSIwcHgiCgkgdmlld0JveD0iMCAwIDI5MS4zNjQgMjkxLjM2NCIgc3R5bGU9ImVuYWJsZS1iYWNrZ3JvdW5kOm5ldyAwIDAgMjkxLjM2NCAyOTEuMzY0OyIgeG1sOnNwYWNlPSJwcmVzZXJ2ZSI+CjxnPgoJPHBhdGggc3R5bGU9ImZpbGw6IzI2QTZEMTsiIGQ9Ik0xNDUuNjgzLDkwLjkzN2MzMC4xNywwLDU0LjYyMiwyNC40NzEsNTQuNjIyLDU0LjYyMmMwLDMwLjE3OS0yNC40NTMsNTQuNjIyLTU0LjYyMiw1NC42MjIKCQljLTMwLjE1MiwwLTU0LjYyMi0yNC40NTMtNTQuNjIyLTU0LjYyMkM5MS4wNjEsMTE1LjQwOCwxMTUuNTMyLDkwLjkzNywxNDUuNjgzLDkwLjkzN3oiLz4KCTxwYXRoIHN0eWxlPSJmaWxsOiMzREIzOUU7IiBkPSJNMTQ1LjY4MywyMTguMzk4Yy0zNi44NTIsMC02Ny4yMzEtMjcuMzg0LTcyLjA3NC02Mi45MjVMMjMuNDAyLDY4LjQxNGwtMC4wNTUtMC4xMjdsLTUuNjE3LDguMTExCgkJYy0xLjM4NCwyLjU0LTIuNzMxLDUuMTE2LTMuOTY5LDcuNzQ3bC0yLjY4Niw2LjI4MmMtMC44OTIsMi4xNjctMS43NzUsNC4zNjEtMi41NjcsNi42Yy0wLjg5MiwyLjQ4NS0xLjY2Niw0Ljk5OC0yLjQwMyw3LjUyCgkJbC0xLjYyLDUuNzk5Yy0wLjY3NCwyLjcwNC0xLjI0Nyw1LjQwOC0xLjc2Niw4LjEzbC0wLjk4Myw1Ljc0NGMtMC40MDEsMi42NjctMC43MSw1LjMxNy0wLjk0Nyw3Ljk2NmwtMC40NzMsNi4zNDUKCQljLTAuMTE4LDIuNDIyLTAuMTY0LDQuODM0LTAuMTY0LDcuMjU2czAuMDM2LDQuODQzLDAuMTY0LDcuMjU2bDAuNDczLDYuMzQ1YzAuMjQ2LDIuNjQ5LDAuNTY0LDUuMzE3LDAuOTQ3LDcuOTY2bDAuOTgzLDUuNzQ0CgkJYzAuNTI4LDIuNzIyLDEuMDkyLDUuNDI2LDEuNzY2LDguMTNsMS42Miw1Ljc5OWMwLjc0NywyLjUwNCwxLjUxMSw1LjAzNCwyLjQwMyw3LjUyYzAuNzkyLDIuMjIxLDEuNjg0LDQuNDA2LDIuNTY3LDYuNgoJCWMwLjg3NCwyLjA5NCwxLjcxMSw0LjE5NywyLjY4Niw2LjI3MmMxLjIzOCwyLjYzMSwyLjU4NSw1LjE4OSwzLjk3OCw3Ljc1NmwxLjgxMiwzLjU2bDAuODM4LDEuMzAyCgkJYzEuNjAyLDIuNzQsMy4zNjgsNS4zNTMsNS4xNTMsNy45NjZsMi44NTksNC4yNTFjMS43MjEsMi4zNDksMy42MDUsNC41ODgsNS40OSw2Ljg0NmwzLjc2OSw0LjUxNQoJCWMxLjYzOSwxLjc5MywzLjM5NiwzLjUwNSw1LjEyNSw1LjIyNmMxLjczLDEuNzM5LDMuNDMyLDMuNTIzLDUuMjM1LDUuMTM0bDQuNTE1LDMuNzg3YzIuMjU4LDEuODY2LDQuNDk3LDMuNzUxLDYuODQ2LDUuNDgKCQlsNC4yMTUsMi44NzdjMi42MjIsMS43OTMsNS4yNTMsMy41Niw3Ljk2Niw1LjE1M2wxLjI5MywwLjgzOGwzLjU1LDEuODEyYzIuNTQ5LDEuMzg0LDUuMTE2LDIuNzU4LDcuNzQ3LDMuOTg3bDAuNzU2LDAuMzkxCgkJbDMuNjg3LDEuNTExYzkuODUsNC4xOTcsMjAuMTM3LDcuMTY1LDMwLjYyNSw5LjEyMmw0MS41NTgtNzIuMDY1QzE1NS45MDcsMjE3Ljg0MywxNTAuODYzLDIxOC4zOTgsMTQ1LjY4MywyMTguMzk4eiIvPgoJPHBhdGggc3R5bGU9ImZpbGw6I0VGQzc1RTsiIGQ9Ik0yOTAuODUxLDEzNS42MzZsLTAuMTkxLTIuNDAzYy0xLjAyOS0xMi4xNjMtNi4wMjctMjkuNzY5LTEwLjE3OC00MS41ODZoLTg1Ljk2NgoJCWMxNC43MDIsMTMuMzE5LDIzLjk5NywzMi41LDIzLjk5Nyw1My45MTJjMCwyMy4xMDUtMTAuNzg4LDQzLjY1Mi0yNy41MzksNTYuOTg5bC01MS4yMDgsODguODE2YzE4LjAwNywwLDQyLjI1LTMuMjUsNTkuNjkzLTEwLjIyMwoJCWwwLjU5Mi0wLjE5MWwwLjkyOS0wLjM5MWM1Ljk0NS0yLjQ2NywxMS44MjYtNS4xNzEsMTcuNTUyLTguNDg1YzUuMTI1LTIuOTUsOS45MjMtNi4yNjMsMTQuNTU3LTkuNzIzbDAuMjY0LTAuMjA5CgkJYzQuNDYxLTMuMzc4LDguNjM5LTYuOTczLDEyLjYyNy0xMC43NjFsMC42MTktMC41MzdsMi4wMTItMi4wNDhjOC44MTItOC44MjIsMTYuMzc4LTE4LjU2MiwyMi41NS0yOS4wNjhsMC43MDEtMS4wMjlsMS4yNjUtMi41NTgKCQljMS45ODUtMy42MDUsMy44MjQtNy4yOTIsNS40OTktMTEuMDQzbDIuMTQ4LTUuMDUzYzEuNDItMy41NiwyLjY1OC03LjE2NSwzLjc5Ni0xMC44MTVsMS41MDItNC42OTgKCQljMS4zOTMtNS4wNzEsMi41MzEtMTAuMTg3LDMuMzc4LTE1LjM2N2wwLjM1NS0yLjk1YzAuNjI4LTQuNDQzLDEuMDc0LTguOTEzLDEuMzAyLTEzLjQyOGwwLjA4Mi0zLjYxNAoJCUMyOTEuMjYxLDE0NC42NzYsMjkxLjE3LDE0MC4xNTIsMjkwLjg1MSwxMzUuNjM2eiIvPgoJPHBhdGggc3R5bGU9ImZpbGw6I0UyNTc0QzsiIGQ9Ik0yNzEuNzc5LDcyLjczYy0zLjA1LTUuMjgtNi40NjQtMTAuMjA1LTEwLjA1MS0xNC45NTdsLTEuMTc0LTEuNDU3CgkJYy0zLjIyMy00LjE2LTYuNjM3LTguMTExLTEwLjI0Mi0xMS44MjZsLTIuNDQtMi41MDRjLTMuOTYtMy44OTYtOC4wNjYtNy41OTMtMTIuMzcyLTEwLjk3bC0wLjk0Ny0wLjgwMQoJCWMtNC43OTgtMy42OTYtOS44MjMtNy4wMTktMTUuMDAzLTEwLjA2bC0xLjAzOC0wLjY5MmwtMy45NDItMi4wNDhjLTIuMjc2LTEuMjI5LTQuNTQzLTIuNDU4LTYuODgyLTMuNTYKCQljLTIuMjQ5LTEuMDQ3LTQuNTE1LTEuOTc2LTYuNzgyLTIuOTEzbC01LjcwOC0yLjI3NmMtMi43NTgtMC45OTItNS41NDQtMS44My04LjMzOS0yLjY4NmwtNC42NDMtMS4zNDcKCQljLTMuMDQxLTAuNzgzLTYuMTA5LTEuNDAyLTkuMTY3LTEuOTk0bC00LjMzMy0wLjg1NmMtMi45ODYtMC40ODItNS45NzItMC43ODMtOC45NjctMS4wNjVsLTQuOTA3LTAuNTAxCgkJYy0yLjU2Ny0wLjE2NC01LjEyNS0wLjE0Ni03LjcxMS0wLjE4MkwxNDAuODEzLDBsLTQuOTE2LDAuMzU1Yy0xMC44NywwLjcyOC0yMS43NCwyLjY2Ny0zMi40MTgsNS44OTlsLTIuMjEyLDAuNjAxCgkJYy0yLjcwNCwwLjg3NC01LjM3MSwxLjkzOS04LjA0OCwyLjk4NmwtNS4xMzQsMi4wM2MtMi4yMDMsMC45MzgtNC4zNzksMi4wMzktNi41NjQsMy4xMjNjLTIuMTEyLDEuMDM4LTQuMjMzLDIuMDg1LTYuMzA5LDMuMjQxCgkJbC0yLjM2NywxLjIxMWwtMy40OTYsMi4yNThjLTIuMDY3LDEuMjc1LTQuMTE1LDIuNTc2LTYuMTI3LDMuOTZjLTEuOTY2LDEuMzc1LTMuOTA1LDIuNzc3LTUuNzk5LDQuMjE1bC01LjAwNywzLjk5NwoJCWMtMi4wNjcsMS43My00LjA3OCwzLjQ4Ny02LjAzNiw1LjMwN2wtMy43MTQsMy42MjNjLTIuMTU4LDIuMTY3LTQuMjcsNC4zNy02LjI2Myw2LjY0NmwtMS4zNTYsMS42MzlsNDEuNDMxLDcxLjg3NAoJCWM5LjUxMy0yOS4xNDEsMzYuODg4LTUwLjIzNCw2OS4yMTYtNTAuMjM0QzE0NS42OTIsNzIuNzMsMjcxLjc3OSw3Mi43MywyNzEuNzc5LDcyLjczeiIvPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+Cjwvc3ZnPgo=";

    String firefox = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgdmVyc2lvbj0iMS4xIiBpZD0iTGF5ZXJfMSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgeD0iMHB4IiB5PSIwcHgiCgkgdmlld0JveD0iMCAwIDI5MS42NzggMjkxLjY3OCIgc3R5bGU9ImVuYWJsZS1iYWNrZ3JvdW5kOm5ldyAwIDAgMjkxLjY3OCAyOTEuNjc4OyIgeG1sOnNwYWNlPSJwcmVzZXJ2ZSI+CjxnPgoJPGc+CgkJPHBhdGggc3R5bGU9ImZpbGw6IzIzOTRCQzsiIGQ9Ik0xNDUuNjYsNC4yNzdjNzUuNTYxLDAsMTM2LjU1Niw2MC45OTUsMTM2LjU1NiwxMzYuNTU2UzIyMS4yMiwyNzcuMzg5LDE0NS42NiwyNzcuMzg5CgkJCVM5LjEwNCwyMTYuMzk0LDkuMTA0LDE0MC44MzNTNzAuMDk5LDQuMjc3LDE0NS42Niw0LjI3N3oiLz4KCQk8cGF0aCBzdHlsZT0iZmlsbDojRUM4ODQwOyIgZD0iTTE2OS4zMjksMjg0LjY3MWM2OS4xODgtMTEuODM1LDEyMS45OS03MS4wMDksMTIxLjk5LTE0My44MzlsLTAuOTEsMS44MjEKCQkJYzEuODIxLTEzLjY1NiwxLjgyMS0yNi40MDEtMC45MS0zNi40MTVjLTAuOTEsOC4xOTMtMS44MjEsMTIuNzQ1LTMuNjQyLDE0LjU2NmMwLTAuOTEsMC05LjEwNC0yLjczMS0yMC4wMjgKCQkJYy0wLjkxLTguMTkzLTIuNzMxLTE2LjM4Ny01LjQ2Mi0yMy42N2MwLjkxLDMuNjQxLDAuOTEsNi4zNzMsMC45MSw5LjEwNGMtMTAuOTI0LTI4LjIyMi0zNi40MTUtNjMuNzI2LTEwMS4wNTEtNjIuODE2CgkJCWMwLDAsMjIuNzU5LDIuNzMxLDMyLjc3MywxOC4yMDdjMCwwLTEwLjkyNC0yLjczMS0xOS4xMTgsMS44MjFjMTAuMDE0LDMuNjQxLDE5LjExOCw4LjE5MywyNi40MDEsMTIuNzQ1aDAuOTEKCQkJYzEuODIxLDAuOTEsMy42NDEsMi43MzEsNS40NjIsMy42NDFjMTMuNjU2LDEwLjAxNCwyNi40MDEsMjMuNjcsMjUuNDksNDAuOTY3Yy0zLjY0MS01LjQ2Mi03LjI4My05LjEwNC0xMi43NDUtMTAuMDE0CgkJCWM2LjM3MywyNC41OCw2LjM3Myw0NC42MDgsMS44MjEsNjAuMDg1Yy0zLjY0MS0xMC45MjQtNi4zNzMtMTYuMzg3LTkuMTA0LTE5LjExOGMzLjY0MSwzMi43NzMtMC45MSw1Ni40NDMtMTUuNDc2LDcxLjkxOQoJCQljMi43MzEtOS4xMDQsMy42NDEtMTcuMjk3LDMuNjQxLTIzLjY3Yy0xNy4yOTcsMjUuNDktMzYuNDE1LDM5LjE0Ni01OC4yNjQsNDAuMDU2Yy04LjE5MywwLTE2LjM4Ny0wLjkxLTI0LjU4LTMuNjQxCgkJCWMtMTAuOTI0LTMuNjQxLTIwLjkzOS0xMC4wMTQtMzAuMDQyLTE5LjExOGMxMy42NTYsMC45MSwyNy4zMTEtMC45MSwzOC4yMzYtNy4yODNsMTguMjA3LTExLjgzNWwwLDAKCQkJYzIuNzMxLTAuOTEsNC41NTItMC45MSw3LjI4MywwYzQuNTUyLTAuOTEsNi4zNzMtMi43MzEsNC41NTItNy4yODNjLTEuODIxLTIuNzMxLTUuNDYyLTUuNDYyLTEwLjAxNC04LjE5MwoJCQljLTkuMTA0LTQuNTUyLTE5LjExOC0zLjY0MS0yOS4xMzIsMi43MzFjLTEwLjAxNCw1LjQ2Mi0xOS4xMTgsNS40NjItMjguMjIyLTAuOTFjLTUuNDYyLTMuNjQxLTExLjgzNS05LjEwNC0xNy4yOTctMTYuMzg3CgkJCWwtMS44MjEtMy42NDFjLTAuOTEsOC4xOTMsMCwxNy4yOTcsMy42NDEsMzAuMDQybDAsMGwwLDBjLTMuNjQxLTExLjgzNS00LjU1Mi0yMS44NDktMy42NDEtMzAuMDQybDAsMAoJCQljMC02LjM3MywyLjczMS0xMC45MjQsOC4xOTMtMTAuOTI0aC0xLjgyMWgyLjczMWM2LjM3MywwLjkxLDEyLjc0NSwxLjgyMSwyMC45MzksNC41NTJjMC45MS03LjI4MywwLTE1LjQ3Ni01LjQ2Mi0yMy42N2wwLDAKCQkJYzcuMjgzLTcuMjgzLDEzLjY1Ni0xMS44MzUsMTkuMTE4LTE0LjU2NmMyLjczMS0wLjkxLDMuNjQxLTMuNjQxLDQuNTUyLTYuMzczbDAsMGwwLDBsMCwwYzEuODIxLTMuNjQxLDAuOTEtNS40NjItMC45MS03LjI4MwoJCQljLTUuNDYyLDAtMTAuMDE0LDAtMTUuNDc2LTAuOTFsMCwwYy0xLjgyMS0wLjkxLTQuNTUyLTIuNzMxLTguMTkzLTUuNDYybC04LjE5My04LjE5M2wtMi43MzEtMS44MjFsMCwwbDAsMGwwLDBsLTAuOTEtMC45MQoJCQlsMC45MS0wLjkxYzAuOTEtNi4zNzMsMi43MzEtMTEuODM1LDUuNDYyLTE2LjM4N2wwLjkxLTAuOTFjMi43MzEtNC41NTIsOC4xOTMtOS4xMDQsMTUuNDc2LTE0LjU2NgoJCQljLTE0LjU2NiwxLjgyMS0yNy4zMTEsOC4xOTMtMzkuMTQ2LDE5LjExOGMtOS4xMDQtMi43MzEtMjAuOTM5LTEuODIxLTMzLjY4NCwzLjY0MWwtMS44MjEsMC45MWwwLDBsMS44MjEtMC45MWwwLDAKCQkJYy04LjE5My0zLjY0MS0xMy42NTYtMTQuNTY2LTE2LjM4Ny0zMi43NzNDMjAuOTM5LDM2LjE0LDE2LjM4Nyw1NS4yNTgsMTYuMzg3LDgxLjY1OWwtMi43MzEsNC41NTJsLTAuOTEsMC45MWwwLDBsMCwwbDAsMAoJCQljLTEuODIxLDIuNzMxLTMuNjQxLDYuMzczLTYuMzczLDEwLjkyNGMtMy42NDEsNy4yODMtNS40NjIsMTIuNzQ1LTUuNDYyLDE4LjIwN2wwLDBsMCwwdjEuODIxbDAsMGMwLDAuOTEsMCwyLjczMSwwLDMuNjQxCgkJCWw4LjE5My02LjM3M2MtMi43MzEsOC4xOTMtNS40NjIsMTYuMzg3LTYuMzczLDI0LjU4djMuNjQxTDAsMTQwLjgzM2MwLDMwLjk1MywxMC4wMTQsNjAuMDg1LDI2LjQwMSw4My43NTRsMC45MSwwLjkxbDAuOTEsMC45MQoJCQljMTEuODM1LDE2LjM4NywyNy4zMTEsMzAuMDQyLDQ1LjUxOSw0MC45NjdjMTIuNzQ1LDcuMjgzLDI2LjQwMSwxMi43NDUsNDAuOTY3LDE2LjM4N2wyLjczMSwwLjkxCgkJCWMyLjczMSwwLjkxLDYuMzczLDAuOTEsOS4xMDQsMS44MjFjMi43MzEsMCw0LjU1MiwwLjkxLDcuMjgzLDAuOTFoMi43MzFoNC41NTJoNC41NTJoMy42NDFoNi4zNzNjMy42NDEsMCw3LjI4My0wLjkxLDEwLjkyNC0wLjkxCgkJCUMxNjYuNTk4LDI4NC42NzEsMTY5LjMyOSwyODQuNjcxLDE2OS4zMjksMjg0LjY3MXogTTI2MS4yNzcsMTA3LjE0OXYwLjkxVjEwNy4xNDlMMjYxLjI3NywxMDcuMTQ5eiIvPgoJPC9nPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+Cjwvc3ZnPgo=";

    String ie = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgdmVyc2lvbj0iMS4xIiBpZD0iTGF5ZXJfMSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgeD0iMHB4IiB5PSIwcHgiCgkgdmlld0JveD0iMCAwIDUwMy40MTcgNTAzLjQxNyIgc3R5bGU9ImVuYWJsZS1iYWNrZ3JvdW5kOm5ldyAwIDAgNTAzLjQxNyA1MDMuNDE3OyIgeG1sOnNwYWNlPSJwcmVzZXJ2ZSI+CjxwYXRoIHN0eWxlPSJmaWxsOiMwMEM4RjY7IiBkPSJNMTc5LjY0OSwyOTIuNDg3YzAsMTcuMDY3LDcuMTg2LDMxLjQzOSwxNS4yNyw0Ni43MDljOC45ODIsMTYuMTY4LDIxLjU1OCwyOC43NDQsMzcuNzI2LDM4LjYyNQoJczMzLjIzNSwxNC4zNzIsNTIuOTk2LDE0LjM3MmMxOC44NjMsMCwzNi44MjgtNC40OTEsNTIuOTk2LTE0LjM3MmMxNi4xNjgtOS44ODEsMjcuODQ2LTI0LjI1MywzNy43MjYtNDAuNDIxaDEyNS43NTQKCWMtMTYuMTY4LDQ1LjgxMS00NC4wMTQsODUuMzMzLTg0LjQzNSwxMTQuMDc3cy04NS4zMzMsNDQuMDE0LTEzNC43MzcsNDQuMDE0Yy0zNi44MjgsMC03Mi43NTgtOC4wODQtMTA1LjA5NS0yNC4yNTMKCWMtNzMuNjU2LDM1LjkzLTEyNi42NTMsMzcuNzI2LTE1OC45ODksNS4zODlDNS4zODksNDY1Ljg0OCwwLDQ0Ny44ODMsMCw0MjMuNjMxczQuNDkxLTUyLjA5OCwxNC4zNzItODEuNzQKCWM5Ljg4MS0zMC41NCwyNS4xNTEtNjIuODc3LDQ3LjYwNy05OS43MDVjMTcuMDY3LTI4Ljc0NCwyOS42NDItNTEuMiw3OC4xNDctOTcuOTA5YzE3Ljk2NS0xNy45NjUsMjYuMDQ5LTI2Ljk0NywzMC41NC0zMS40MzkKCWMtNDMuMTE2LDIwLjY2LTk4LjgwNyw1OS4yODQtMTQzLjcxOSw5OC44MDdjMjYuOTQ3LTY2LjQ3LDY0LjY3NC0xMDAuNjA0LDExMC40ODQtMTI5LjM0NwoJYzQ5LjQwNC0yOS42NDIsODkuODI1LTQ5LjQwNCwxNDMuNzE5LTQ5LjQwNGM1LjM4OSwwLDEwLjc3OSwwLDE3LjA2NywwLjg5OEMzMzcuNzQsMTUuODI3LDM3My42Nyw2Ljg0NSw0MDYuMDA3LDQuMTUKCWMzMi4zMzctMS43OTYsNTQuNzkzLDMuNTkzLDY4LjI2NywxNi4xNjhjMjYuOTQ3LDI2Ljk0NywyOC43NDQsNzAuMDYzLDcuMTg2LDEyOC40NDljMjAuNjYsMzUuOTMsMjEuNTU4LDc2LjM1MSwyMS41NTgsMTE3LjY3CgljMCw5Ljg4MSwwLjg5OCwxOC44NjMsMCwyNi45NDdIMzg2LjI0NkgxNzkuNjQ5VjI5Mi40ODd6Ii8+CjxnPgoJPHBhdGggc3R5bGU9ImZpbGw6I0ZGRkZGRjsiIGQ9Ik0xNDMuNzE5LDQ0NS4xODljLTQ2LjcwOS0yOC43NDQtNjIuODc3LTUyLjA5OC04MS43NC0xMDMuMjk4CgkJYy0zMS40MzksNjEuMDgxLTQwLjQyMSw4OC45MjYtMTcuMDY3LDExMi4yODFDNjUuNTcyLDQ3NC44MzEsODkuODI1LDQ2OS40NDEsMTQzLjcxOSw0NDUuMTg5Ii8+Cgk8cGF0aCBzdHlsZT0iZmlsbDojRkZGRkZGOyIgZD0iTTM3Ny4yNjMsMjI5LjYxYy0xLjc5Ni0yNS4xNTEtMTAuNzc5LTQ3LjYwNy0yOS42NDItNjQuNjc0CgkJYy0xOC44NjMtMTcuOTY1LTQxLjMxOS0yNS4xNTEtNjYuNDctMjUuMTUxYy0yNi4wNDksMC00Ny42MDcsNy4xODYtNjYuNDcsMjUuMTUxcy0zMi4zMzcsMzkuNTIzLTM0LjEzMyw2NC42NzRIMzc3LjI2M3oiLz4KCTxwYXRoIHN0eWxlPSJmaWxsOiNGRkZGRkY7IiBkPSJNMzY2LjQ4NCw0OS4wNjJjNDMuMTE2LDIxLjU1OCw1MC4zMDIsMjUuMTUxLDgyLjYzOSw2My43NzUKCQljMTAuNzc5LTI5LjY0MiwxMi41NzUtMzcuNzI2LDguMDg0LTQ2LjcwOWMtNC40OTEtMTAuNzc5LTEzLjQ3NC0xOC44NjMtMjMuMzU0LTI0LjI1MwoJCUM0MTcuNjg0LDMyLjg5NCw0MDYuOTA1LDMyLjg5NCwzNjYuNDg0LDQ5LjA2MiIvPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+Cjwvc3ZnPgo=";

    String mac = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTYuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+CjxzdmcgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB4PSIwcHgiIHk9IjBweCIKCSB3aWR0aD0iMzUuNjc2cHgiIGhlaWdodD0iMzUuNjc2cHgiIHZpZXdCb3g9IjAgMCAzNS42NzYgMzUuNjc2IiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCAzNS42NzYgMzUuNjc2OyIKCSB4bWw6c3BhY2U9InByZXNlcnZlIj4KPGc+Cgk8cGF0aCBkPSJNMzIuMjk1LDI2LjIwMmMtMC4wOTYsMC4yNzEtMC4xODksMC41NjItMC4yOTYsMC44NDhjLTAuODg2LDIuMzk5LTIuMjMxLDQuNTI5LTMuODYzLDYuNDgxCgkJYy0wLjMxNSwwLjM4LTAuNjgyLDAuNzI0LTEuMDYxLDEuMDQzYy0wLjc0OSwwLjYzNC0xLjYxMSwxLjAxNy0yLjYwOCwxLjA1MmMtMC43NDksMC4wMjQtMS40NjgtMC4xMTItMi4xNjEtMC4zOTQKCQljLTAuNTAyLTAuMjA1LTAuOTk2LTAuNDM0LTEuNTA1LTAuNjE5Yy0xLjY1Mi0wLjYtMy4yOTUtMC41MjEtNC45MiwwLjEyMWMtMC41ODYsMC4yMzItMS4xNjQsMC40ODgtMS43NjEsMC42ODkKCQljLTAuNjkyLDAuMjMyLTEuNDEsMC4zMjYtMi4xNDEsMC4xODhjLTAuNjM4LTAuMTE5LTEuMTk2LTAuNDE2LTEuNzE0LTAuNzk5Yy0wLjY0My0wLjQ3Ni0xLjE4My0xLjA1Ni0xLjY4OC0xLjY3CgkJYy0yLjM5MS0yLjkxNi0zLjk5Ni02LjIxMy00Ljc3MS05LjkwNmMtMC4zMzQtMS41ODgtMC40OTQtMy4xODktMC4zOTYtNC44MTJjMC4xMTUtMS45NDYsMC41NjctMy43OTksMS42MDctNS40NjkKCQljMS4zMDUtMi4wOTksMy4xNDYtMy40NzQsNS41NjgtNC4wNDFjMS40NTctMC4zNDMsMi44NzQtMC4yMDMsNC4yNjMsMC4zMzJjMC43MzEsMC4yOCwxLjQ2NCwwLjU1NywyLjE5OCwwLjgzMgoJCWMwLjY3NiwwLjI1NCwxLjM0OSwwLjI1NCwyLjAyNi0wLjAwNWMwLjc0OS0wLjI4NiwxLjQ5OS0wLjU3MSwyLjI1MS0wLjg1YzAuNzcxLTAuMjgxLDEuNTU1LTAuNTExLDIuMzczLTAuNTc2CgkJYzEuMjI0LTAuMDk5LDIuNDE4LDAuMDYsMy41OCwwLjQ0OWMxLjY0NywwLjU1MSwyLjk4NywxLjUyNiwzLjk5OSwyLjk0NmMwLjAyNywwLjAzOSwwLjA1NywwLjA4MiwwLjA3NywwLjExMwoJCWMtMi41NTIsMS43NzktNC4wMDUsNC4xMjktMy43OTQsNy4zMTFDMjcuNzcxLDIyLjY1LDI5LjQ4NSwyNC44MDQsMzIuMjk1LDI2LjIwMnogTTE3Ljk4LDguMjUzCgkJYzAuNzUzLDAuMDIsMS40NzctMC4xMjUsMi4xNzQtMC40MDJjMy4xNzktMS4yNjIsNC44NDEtNC42MjUsNC43OTEtNy4xOTdDMjQuOTQxLDAuNDQ3LDI0LjkyNywwLjI0LDI0LjkxOCwwCgkJYy0wLjMyNywwLjA0OS0wLjYyNSwwLjA3Mi0wLjkxMSwwLjE0NGMtMi4zMjEsMC41NjktNC4xMDcsMS44NjQtNS4yODEsMy45NjFjLTAuNjg3LDEuMjI4LTEuMDY5LDIuNTMyLTAuOTUyLDMuOTU3CgkJQzE3Ljc4Miw4LjIxMywxNy44MzcsOC4yNTEsMTcuOTgsOC4yNTN6Ii8+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPC9zdmc+Cg==";

    String opera = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgdmVyc2lvbj0iMS4xIiBpZD0iTGF5ZXJfMSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgeD0iMHB4IiB5PSIwcHgiCgkgdmlld0JveD0iMCAwIDI5MS40OTIgMjkxLjQ5MiIgc3R5bGU9ImVuYWJsZS1iYWNrZ3JvdW5kOm5ldyAwIDAgMjkxLjQ5MiAyOTEuNDkyOyIgeG1sOnNwYWNlPSJwcmVzZXJ2ZSI+CjxnPgoJPHBhdGggc3R5bGU9ImZpbGw6I0UyNTc0QzsiIGQ9Ik0yMjQuMTM4LDIyLjkzMkMyMDIuNTcxLDguMTY2LDE3NS44NzksMCwxNDUuNzgyLDBjLTI3LjUzLDAtNTIuMjAxLDYuODEtNzIuNzMsMTkuMjYzCgkJQzMzLjUzNCw0Mi45OTcsOS4xOTksODcuNDIzLDkuMTk5LDE0NC4zNDljMCw3Ni45OTksNTQuNjEzLDE0Ny4xNDMsMTM2LjU1NiwxNDcuMTQzYzgxLjkzMywwLDEzNi41MzgtNzAuMTI2LDEzNi41MzgtMTQ3LjE0MwoJCUMyODIuMjg0LDkwLjIyNywyNjAuMjg5LDQ3LjQwMywyMjQuMTM4LDIyLjkzMnogTTE0NS43NzMsMjcuMTkzYzQzLjk1MywwLDU0LjU4Niw2MS4wNjgsNTQuNTg2LDExNi41MzcKCQljMCw1MS40LTYuNjY0LDEyMC4xNzgtNTQuMDU4LDEyMC4xNzhzLTU1LjIxNC02OS40NTItNTUuMjE0LTEyMC44NDNDOTEuMDk2LDg3LjU5NiwxMDEuODExLDI3LjE5MywxNDUuNzczLDI3LjE5M3oiLz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8L3N2Zz4K";

    String safari = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgdmVyc2lvbj0iMS4xIiBpZD0iTGF5ZXJfMSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgeD0iMHB4IiB5PSIwcHgiCgkgdmlld0JveD0iMCAwIDI5MS4zMiAyOTEuMzIiIHN0eWxlPSJlbmFibGUtYmFja2dyb3VuZDpuZXcgMCAwIDI5MS4zMiAyOTEuMzI7IiB4bWw6c3BhY2U9InByZXNlcnZlIj4KPGc+Cgk8Zz4KCQk8cGF0aCBzdHlsZT0iZmlsbDojMjZBNkQxOyIgZD0iTTE0NS42NiwwQzY1LjU0NywwLDAsNjUuNTQ3LDAsMTQ1LjY2czY1LjU0NywxNDUuNjYsMTQ1LjY2LDE0NS42NnMxNDUuNjYtNjUuNTQ3LDE0NS42Ni0xNDUuNjYKCQkJUzIyNS43NzIsMCwxNDUuNjYsMHogTTE0NS42NiwyNjQuMDA4Yy02NS41NDcsMC0xMTguMzQ4LTUyLjgwMi0xMTguMzQ4LTExOC4zNDhTODAuMTEzLDI3LjMxMSwxNDUuNjYsMjcuMzExCgkJCVMyNjQuMDA4LDgwLjExMywyNjQuMDA4LDE0NS42NlMyMTEuMjA2LDI2NC4wMDgsMTQ1LjY2LDI2NC4wMDh6Ii8+CgkJPHBhdGggc3R5bGU9ImZpbGw6I0UyNTc0QzsiIGQ9Ik0yMjEuMjIsNzAuMDk5bC01Ni40NDMsOTQuNjc5bC0zOC4yMzYtMzguMjM2QzEyNi41NDIsMTI2LjU0MiwyMjEuMjIsNzAuMDk5LDIyMS4yMiw3MC4wOTl6Ii8+CgkJPHBhdGggc3R5bGU9ImZpbGw6I0U0RTdFNzsiIGQ9Ik03MC4wOTksMjIxLjIybDk0LjY3OS01Ni40NDNsLTM4LjIzNi0zOC4yMzZDMTI2LjU0MiwxMjYuNTQyLDcwLjA5OSwyMjEuMjIsNzAuMDk5LDIyMS4yMnoiLz4KCTwvZz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8L3N2Zz4K";

    String ubantu = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPCEtLSBHZW5lcmF0b3I6IEFkb2JlIElsbHVzdHJhdG9yIDE2LjAuNCwgU1ZHIEV4cG9ydCBQbHVnLUluIC4gU1ZHIFZlcnNpb246IDYuMDAgQnVpbGQgMCkgIC0tPgo8IURPQ1RZUEUgc3ZnIFBVQkxJQyAiLS8vVzNDLy9EVEQgU1ZHIDEuMS8vRU4iICJodHRwOi8vd3d3LnczLm9yZy9HcmFwaGljcy9TVkcvMS4xL0RURC9zdmcxMS5kdGQiPgo8c3ZnIHZlcnNpb249IjEuMSIgaWQ9ImNpcmNsZV9vZl9mcmllbmRzX194NUZfX29yYW5nZSIKCSB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB4PSIwcHgiIHk9IjBweCIgd2lkdGg9IjQyNS4xOTdweCIKCSBoZWlnaHQ9IjQyNS4xOTdweCIgdmlld0JveD0iMCAwIDQyNS4xOTcgNDI1LjE5NyIgZW5hYmxlLWJhY2tncm91bmQ9Im5ldyAwIDAgNDI1LjE5NyA0MjUuMTk3IiB4bWw6c3BhY2U9InByZXNlcnZlIj4KPGc+Cgk8cGF0aCBmaWxsPSIjRTk1NDIwIiBkPSJNMzU0LjMzMSwyMTIuNTk1YzAsNzguMjc5LTYzLjQ1LDE0MS43MzUtMTQxLjcyOSwxNDEuNzM1Yy03OC4yNzksMC0xNDEuNzM1LTYzLjQ1Ni0xNDEuNzM1LTE0MS43MzUKCQljMC03OC4yNzQsNjMuNDU3LTE0MS43MjgsMTQxLjczNS0xNDEuNzI4QzI5MC44ODEsNzAuODY3LDM1NC4zMzEsMTM0LjMyLDM1NC4zMzEsMjEyLjU5NXoiLz4KCTxwYXRoIGZpbGw9IiNGRkZGRkYiIGQ9Ik0xMzkuMDQzLDIxMi41OTVjMCwxMC4wNjQtOC4xNTksMTguMjI1LTE4LjIzLDE4LjIyNWMtMTAuMDU5LDAtMTguMjE4LTguMTYtMTguMjE4LTE4LjIyNQoJCWMwLTEwLjA2LDguMTU5LTE4LjIxOSwxOC4yMTgtMTguMjE5QzEzMC44ODQsMTk0LjM3NiwxMzkuMDQzLDIwMi41MzUsMTM5LjA0MywyMTIuNTk1eiBNMjQyLjcxNywzMDEuMjAxCgkJYzUuMDMzLDguNzA5LDE2LjE3MywxMS42OTYsMjQuODg5LDYuNjdjOC43MTUtNS4wMzMsMTEuNzAxLTE2LjE3OSw2LjY2OS0yNC44OTVjLTUuMDMyLTguNzE1LTE2LjE3My0xMS42OTUtMjQuODg4LTYuNjYzCgkJQzI0MC42NzEsMjgxLjM0NiwyMzcuNjg1LDI5Mi40ODYsMjQyLjcxNywzMDEuMjAxeiBNMjc0LjI3NCwxNDIuMjE5YzUuMDMyLTguNzE3LDIuMDUyLTE5Ljg2LTYuNjY5LTI0Ljg4NwoJCWMtOC43MS01LjAzMi0xOS44NTUtMi4wNDYtMjQuODg5LDYuNjY3Yy01LjAzMiw4LjcxNS0yLjA0NiwxOS44NTcsNi42NywyNC44ODlDMjU4LjEwMiwxNTMuOTIsMjY5LjI0OCwxNTAuOTM0LDI3NC4yNzQsMTQyLjIxOXoKCQkgTTIxMi42MDIsMTYwLjYzMmMyNy4xNTMsMCw0OS40MzQsMjAuODE0LDUxLjc2MSw0Ny4zNjRsMjYuMzcyLTAuNDE2Yy0xLjI1Mi0xOS43MjctOS44MDktMzcuNDY5LTIyLjk5NS01MC41NTEKCQljLTYuOTgsMi42OTMtMTUuMDc5LDIuMzI3LTIyLjA2Ni0xLjcxYy02Ljk5Mi00LjAzNy0xMS4zNTktMTAuODcxLTEyLjUxNC0xOC4yNzVjLTYuNTU0LTEuNzgtMTMuNDQ4LTIuNzMzLTIwLjU1OC0yLjczMwoJCWMtMTIuNDcxLDAtMjQuMjU5LDIuOTE2LTM0LjcyNyw4LjEwM2wxMi44MzIsMjMuMDQzQzE5Ny4zNTcsMTYyLjM2NywyMDQuNzg0LDE2MC42MzIsMjEyLjYwMiwxNjAuNjMyeiBNMTYwLjYzMywyMTIuNTk1CgkJYzAtMTcuNTc3LDguNzM0LTMzLjEyMSwyMi4wOTctNDIuNTJsLTEzLjU0LTIyLjYzNGMtMTUuNjg0LDEwLjQ3NC0yNy4zNjcsMjYuNDUxLTMyLjI5Niw0NS4xODMKCQljNS44MzMsNC42OTcsOS41NywxMS44OTcsOS41NywxOS45NzJjMCw4LjA4LTMuNzM4LDE1LjI4LTkuNTcsMTkuOTc4YzQuOTI5LDE4LjczMSwxNi42MTIsMzQuNzA4LDMyLjI5Niw0NS4xODhsMTMuNTQtMjIuNjM0CgkJQzE2OS4zNjcsMjQ1LjcyMiwxNjAuNjMzLDIzMC4xODQsMTYwLjYzMywyMTIuNTk1eiBNMjEyLjYwMiwyNjQuNTY4Yy03LjgxNywwLTE1LjI0NC0xLjczNC0yMS44OTUtNC44M2wtMTIuODMyLDIzLjA0MwoJCWMxMC40NjgsNS4xOTEsMjIuMjU1LDguMTA0LDM0LjcyNyw4LjEwNGM3LjEwOSwwLDE0LjAwNC0wLjk0NiwyMC41NTgtMi43MjljMS4xNTQtNy40MDksNS41MjEtMTQuMjQzLDEyLjUxNC0xOC4yNzMKCQljNi45ODctNC4wMzcsMTUuMDg2LTQuNDA0LDIyLjA2Ni0xLjcxMWMxMy4xODctMTMuMDg4LDIxLjc0My0zMC44MywyMi45OTUtNTAuNTU3bC0yNi4zNzItMC40MDkKCQlDMjYyLjAzNSwyNDMuNzQ5LDIzOS43NTUsMjY0LjU2OCwyMTIuNjAyLDI2NC41Njh6Ii8+CjwvZz4KPC9zdmc+Cg==";

    String win10 = "PD94bWwgdmVyc2lvbj0iMS4wIiA/PjxzdmcgaWQ9IkxhZ2VyXzEiIHN0eWxlPSJlbmFibGUtYmFja2dyb3VuZDpuZXcgMCAwIDEyOCAxMjg7IiB2ZXJzaW9uPSIxLjEiIHZpZXdCb3g9IjAgMCAxMjggMTI4IiB4bWw6c3BhY2U9InByZXNlcnZlIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIj48c3R5bGUgdHlwZT0idGV4dC9jc3MiPgoJLnN0MHtmaWxsOiMwMEFFRjA7fQoJLnN0MXtmaWxsOiNGRkZGRkY7fQo8L3N0eWxlPjxnPjxnPjxnPjxjaXJjbGUgY2xhc3M9InN0MCIgY3g9IjY0IiBjeT0iNjQiIHI9IjUwIi8+PC9nPjwvZz48cGF0aCBjbGFzcz0ic3QxIiBkPSJNODYsNjIuN2wtMjcsMC4ydi0yMEw4NiwzOVY2Mi43eiBNMzYuMiw2NS4xdjE3bDIwLjQsMi44VjY1LjJMMzYuMiw2NS4xeiBNNTguOSw2NS40djE5LjhMODYsODlWNjUuNEg1OC45eiAgICBNMzYuMiw0Ni4xdjE3TDU2LjUsNjNWNDMuNEwzNi4yLDQ2LjF6Ii8+PC9nPjwvc3ZnPg==";

    public static void main(String[] args) {
        ReportLibraryFiles file = new ReportLibraryFiles();
        file.createLibrary();
    }

    public void decoder(String base64Image, String pathFile) {
        try (FileOutputStream imageOutFile = new FileOutputStream(pathFile)) {
            // Converting a Base64 String into Image byte array
            byte[] imageByteArray = Base64.getDecoder().decode(base64Image.getBytes(StandardCharsets.UTF_8));
            imageOutFile.write(imageByteArray);
        } catch (FileNotFoundException e) {
            System.out.println("Image not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the Image " + ioe);
        }
    }

    public String encoder(String imagePath) {
        String base64Image = "";
        File file = new File(imagePath);
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            // Reading a Image file from file system
            byte imageData[] = new byte[(int) file.length()];
            imageInFile.read(imageData);
            base64Image = Base64.getEncoder().encodeToString(imageData);
        } catch (FileNotFoundException e) {
            System.out.println("Image not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the Image " + ioe);
        }


        return base64Image;
    }


    public void createLibrary() {
        ReportBuilder builder = new ReportBuilder();
        generateReportLibraryStructure();
        builder.writeReportFile("./htmlReport/lib/bootstrap/dist/css/bootstrap.css", getFileContent("/Resources/lib/bootstrap/dist/css/bootstrap.css"));
        builder.writeReportFile("./htmlReport/lib/bootstrap/dist/css/bootstrap.min.css", getFileContent("/Resources/lib/bootstrap/dist/css/bootstrap.min.css"));
        builder.writeReportFile("./htmlReport/lib/bootstrap/dist/js/bootstrap.js", getFileContent("/Resources/lib/bootstrap/dist/js/bootstrap.js"));
        builder.writeReportFile("./htmlReport/lib/bootstrap/dist/js/bootstrap.min.js", getFileContent("/Resources/lib/bootstrap/dist/js/bootstrap.min.js"));
        builder.writeReportFile("./htmlReport/lib/build/css/custom.css", getFileContent("/Resources/lib/build/css/custom.css"));
        builder.writeReportFile("./htmlReport/lib/build/css/custom.min.css", getFileContent("/Resources/lib/build/css/custom.min.css"));
        builder.writeReportFile("./htmlReport/lib/jquery/dist/jquery.js", getFileContent("/Resources/lib/jquery/dist/jquery.js"));
        builder.writeReportFile("./htmlReport/lib/jquery/dist/jquery.min.js", getFileContent("/Resources/lib/jquery/dist/jquery.min.js"));
        try {
            String chromePath = "./htmlReport/lib/Icon/chrome.svg";
            String firefoxPath = "./htmlReport/lib/Icon/firefox.svg";
            String iePath = "./htmlReport/lib/Icon/ie.svg";
            String macPath = "./htmlReport/lib/Icon/mac.svg";
            String operaPath = "./htmlReport/lib/Icon/opera.svg";
            String safariPath = "./htmlReport/lib/Icon/safari.svg";
            String ubantuPath = "./htmlReport/lib/Icon/ubantu.svg";
            String win10Path = "./htmlReport/lib/Icon/Win10.svg";
            generatefile(chromePath);
            generatefile(firefoxPath);
            generatefile(iePath);
            generatefile(macPath);
            generatefile(operaPath);
            generatefile(safariPath);
            generatefile(ubantuPath);
            generatefile(win10Path);
            decoder(chrome, chromePath);
            decoder(firefox, firefoxPath);
            decoder(ie, iePath);
            decoder(safari, safariPath);
            decoder(mac, macPath);
            decoder(opera, operaPath);
            decoder(ubantu, ubantuPath);
            decoder(win10, win10Path);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public StringBuilder getFileContent(String respath) {
        InputStream in = ReportBuilder.class.getResourceAsStream(respath);
        if (in == null)
            try {
                throw new Exception("resource not found: " + respath);
            } catch (Exception e) {
                e.printStackTrace();
            }


        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(in));
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb;
    }

    public void generateReportLibraryStructure() {
        generateDir("./htmlReport/lib");
        generateDir("./htmlReport/lib/bootstrap");
        generateDir("./htmlReport/lib/bootstrap/dist");
        generateDir("./htmlReport/lib/bootstrap/dist/css");
        generateDir("./htmlReport/lib/bootstrap/dist/js");
        generateDir("./htmlReport/lib/build");

        generateDir("./htmlReport/lib/build/css");


        generateDir("./htmlReport/lib/icon");
        generateDir("./htmlReport/lib/jquery");
        generateDir("./htmlReport/lib/jquery/dist");

    }

    public void generateDir(String path) {


        File lib = new File(path);

        if (!lib.exists()) {
            lib.mkdir();
        }


    }


    public void generatefile(String path) {


        File lib = new File(path);

        if (!lib.exists()) {
            try {
                lib.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


}
