- ******css动态计算宽度******
    - width: calc(100% - 24px);
    
- ******不处理点击事件******
    - style="pointer-events: none;" 
    
- **过滤器，动态设置显示的绑定字段**
    -  ```js
        <div v-bind:id="rawId | formatId"></div>
        filters: {
            capitalize: function (value) {
                if (!value) return ''
                value = value.toString()
                return value.charAt(0).toUpperCase() + value.slice(1)
            }
        }
        ```
       
- **vue双向绑定，取消动态更新文字**
  - 使用 v-once 
  - ```js
    <span v-once>这个将不会改变: {{ message }}</span>
    ```
  


