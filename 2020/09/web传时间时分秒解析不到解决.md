添加时间转换
```java
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new SpecialDateEditor());
    }
```
