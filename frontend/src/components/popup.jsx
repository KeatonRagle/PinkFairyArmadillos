import { createPortal } from 'react-dom'
import { useEffect, useState } from 'react'
import '../styling/popup.css'

export default function Popup({index, isOpen, isLoading, errorMessage, placeholder, title, onClose, onSubmit}) {
    const [text, setText] = useState('')

    const handleChange = (event) => {
        const { value } = event.target
		setText(value)
    }

    const handlePost = () => {
        if (text.trim()) {
            onSubmit(index, text)
            onClose()
        }
    }

    useEffect(() => {
        if (isOpen) setText('')
    }, [isOpen])

    useEffect(() => {
        if (isOpen) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'unset';
        }

        return () => {
            document.body.style.overflow = 'unset';
        };
    }, [isOpen]); 

    if (!isOpen) return null
    
    return createPortal(
        <div className="popup-overlay" onClick={onClose}>
            {/* stopPropagation prevents the modal from closing when you click inside the white box */}
            <div className="popup-content" onClick={(e) => e.stopPropagation()}>
                <form onSubmit={handlePost}>
                    <h3>{title}</h3>
                    
                    <textarea 
                        autoFocus 
                        name="content" 
                        placeholder={placeholder} 
                        rows="8" 
                        onChange={handleChange} 
                        value={text} 
                        required 
                    />

                    <div className="popup-actions">
                        <button className="cancel-btn" type="button" onClick={onClose}>
                            Cancel
                        </button>
                        <button type="submit" disabled={isLoading || !text.trim()}>
                            {isLoading ? 'Posting...' : 'Post'}
                        </button>
                    </div>

                    {errorMessage && <p className="popup-error">{errorMessage}</p>}
                </form>
            </div>
        </div>,
        document.body
    );
}