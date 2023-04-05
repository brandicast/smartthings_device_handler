# PST02-A

## Configuration

- 直接在App設定無效，需要先wake up device。 方法是按一下device內的trigger一下，會wake up 10 secs，在那10 secs內，完成設定。
  - 但是設定完會沒有反應，目前方法是把電池拆下再裝回。

- size都是1

- auto report的部份，除了PVR單獨設定之外，temperature、illumuniation、battery的單位都是minutes  
  - 所以default value = 12，再乘上第20個設定的 30 = 360 mins = 6 hours

- Temperature Diff (第21個設定) default 是 enable。 每一分鐘檢查一次，只要溫差有超過 0.56 (攝氏)，就會丟report出來。