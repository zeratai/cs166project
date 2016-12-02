create index Users_Index
on Users
using btree
(login);

create index Menu_Index
on Menu
using btree
(itemName);


create index Orders_Index
on Orders
using btree
(orderid);


create index ItemStatus_Index
on ItemStatus
using btree
(orderid);
